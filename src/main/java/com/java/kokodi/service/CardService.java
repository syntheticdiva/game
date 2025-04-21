package com.java.kokodi.service;

import com.java.kokodi.dto.TurnDto;
import com.java.kokodi.entity.Card;
import com.java.kokodi.entity.GameSession;
import com.java.kokodi.entity.Turn;
import com.java.kokodi.entity.User;
import com.java.kokodi.enums.CardType;
import com.java.kokodi.enums.GameStatus;
import com.java.kokodi.exception.GameException;
import com.java.kokodi.mapper.CardMapper;
import com.java.kokodi.mapper.TurnMapper;
import com.java.kokodi.repository.CardRepository;
import com.java.kokodi.repository.TurnRepository;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serial;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {
    private final CardRepository cardRepository;
    private final TurnRepository turnRepository;
    private final UserService userService;
    private final CardMapper cardMapper;
    private final TurnMapper turnMapper;
    @Transactional
    public void initializeDeck(GameSession gameSession) {
        // Удаляем существующие карты из БД
        cardRepository.deleteAllByGameSession(gameSession);

        List<Card> deck = new ArrayList<>();

        // Создаем базовые карты
        deck.add(createCard("Small Points", CardType.POINTS, 2, gameSession));
        deck.add(createCard("Block", CardType.ACTION, 1, gameSession));
        deck.add(createCard("Steal", CardType.ACTION, 3, gameSession));
        deck.add(createCard("Double Down", CardType.ACTION, 2, gameSession));
        deck.add(createCard("Medium Points", CardType.POINTS, 7, gameSession));
        deck.add(createCard("Mega Points", CardType.POINTS, 10, gameSession));

        // Перемешиваем колоду
        Collections.shuffle(deck);

        // Устанавливаем порядковые индексы
        for(int i = 0; i < deck.size(); i++) {
            deck.get(i).setOrderIndex(i);
        }

        // Сохраняем карты в БД
        List<Card> savedCards = cardRepository.saveAll(deck);

        // Обновляем колоду в игровой сессии
        gameSession.getDeck().clear();
        gameSession.getDeck().addAll(savedCards);
    }
    private Card createCard(String name, CardType type, int value, GameSession gameSession) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setValue(value);
        card.setGameSession(gameSession);
        card.setPlayed(false);
        card.setOrderIndex(0); // Временное значение, будет перезаписано после shuffle
        return card;
    }
//    @Transactional
//    public void initializeDeck(GameSession gameSession) {
//        List<Card> deck = List.of(
//                createCard("Small Points", CardType.POINTS, 2, gameSession),
//                createCard("Medium Points", CardType.POINTS, 5, gameSession),
//                createCard("Big Points", CardType.POINTS, 8, gameSession),
//                createCard("Block", CardType.ACTION, 0, gameSession), // Блокирует следующего игрока
//                createCard("Steal", CardType.ACTION, 0, gameSession), // Кража 3 очков
//                createCard("Swap", CardType.ACTION, 0, gameSession), // Обмен очками
//                createCard("Double", CardType.ACTION, 0, gameSession), // Удвоение очков
//                createCard("Bomb", CardType.ACTION, 0, gameSession) // Сброс очков всех игроков до 0
//        );
//
//        Collections.shuffle(deck);
//        gameSession.setDeck(deck);
//        cardRepository.saveAll(deck);
//    }
//    private Card createCard(String name, CardType type, int value, GameSession gameSession){
//        Card card = new Card();
//        card.setName(name);
//        card.setType(type);
//        card.setValue(value);
//        card.setGameSession(gameSession);
//        return card;
//    }
@Transactional
public Card drawCard(GameSession gameSession) {
    // Получаем все несыгранные карты, отсортированные по orderIndex
    List<Card> availableCards = gameSession.getDeck().stream()
            .filter(card -> !card.isPlayed())
            .sorted(Comparator.comparingInt(Card::getOrderIndex))
            .collect(Collectors.toList());

    if (availableCards.isEmpty()) {
        reshuffleDiscardedCards(gameSession);
        availableCards = gameSession.getDeck().stream()
                .filter(card -> !card.isPlayed())
                .sorted(Comparator.comparingInt(Card::getOrderIndex))
                .collect(Collectors.toList());

        if (availableCards.isEmpty()) {
            throw new GameException("No cards available after reshuffle");
        }
    }

    // Берём карту с наименьшим orderIndex (верхняя карта)
    Card card = availableCards.get(0);
    card.setPlayed(true);
    cardRepository.save(card);

    return card;
}
    private void reshuffleDiscardedCards(GameSession gameSession) {
        List<Card> deck = gameSession.getDeck();
        Collections.shuffle(deck);

        // Обновляем индексы порядка
        for (int i = 0; i < deck.size(); i++) {
            deck.get(i).setOrderIndex(i);
            deck.get(i).setPlayed(false);
        }

        cardRepository.saveAll(deck);
        log.info("Deck reshuffled for game {}", gameSession.getId());
    }
    @Transactional
    public TurnDto applyCardEffect(GameSession gameSession, Card card, User user) {
        card.setPlayed(true);
        card.setPlayedBy(user);
        cardRepository.save(card);

        Turn turn = new Turn();
        turn.setGameSession(gameSession);
        turn.setPlayer(user);
        turn.setCard(card);

        int scoreBefore = userService.getUserScore(user, gameSession);
        int scoreAfter = scoreBefore;
        String action;

        if (card.getType() == CardType.POINTS) {
            scoreAfter = userService.addScore(user, gameSession, card.getValue());
            action = String.format("Player %s gained %d points", user.getName(), card.getValue());
        } else {
            switch (card.getName()) {
                case "Block":
                    gameSession.setBlockNextPlayer(true);
                    action = String.format("Player %s blocked next player", user.getName());
                    break;

                case "Steal":
                    User opponent = chooseRandomOpponent(gameSession, user);

                    // Получаем текущие очки через сервис
                    int opponentScore = userService.getUserScore(opponent, gameSession);
                    int stolenPoints = Math.min(3, opponentScore); // Используем явное значение 3

                    // Обновляем очки
                    userService.addScore(opponent, gameSession, -stolenPoints);
                    scoreAfter = userService.addScore(user, gameSession, stolenPoints);

                    action = String.format("Игрок %s украл %d очков у %s",
                            user.getName(), stolenPoints, opponent.getName());
                    break;
                case "Double Down":
                    int currentScore = userService.getUserScore(user, gameSession);
                    int pointsToAdd = Math.min(currentScore, 30 - currentScore);
                    scoreAfter = userService.addScore(user, gameSession, pointsToAdd);
                    action = String.format("Player %s doubled their score to %d",
                            user.getName(), scoreAfter);
                    break;

                default:
                    throw new GameException("Unknown card: " + card.getName());
            }
        }

        turn.setAction(action);
        turn.setScoreBefore(scoreBefore);
        turn.setScoreAfter(scoreAfter);
        turnRepository.save(turn);

        return turnMapper.toDto(turn);
    }

    private User chooseRandomOpponent(GameSession gameSession, User currentUser) {
        List<User> opponents = gameSession.getPlayers().stream()
                .filter(p -> !p.getId().equals(currentUser.getId()))
                .collect(Collectors.toList());

        if (opponents.isEmpty()) {
            throw new GameException("Не найдено оппонентов для кражи");
        }

        return opponents.get(new Random().nextInt(opponents.size()));
    }
}

