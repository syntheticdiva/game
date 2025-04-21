package com.java.kokodi.service;

import com.java.kokodi.dto.TurnDto;
import com.java.kokodi.entity.Card;
import com.java.kokodi.entity.GameSession;
import com.java.kokodi.entity.Turn;
import com.java.kokodi.entity.User;
import com.java.kokodi.enums.CardType;
import com.java.kokodi.exception.GameException;
import com.java.kokodi.mapper.CardMapper;
import com.java.kokodi.mapper.TurnMapper;
import com.java.kokodi.repository.CardRepository;
import com.java.kokodi.repository.TurnRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    /**
     * Инициализирует новую колоду карт для указанной игровой сессии.
     * <p>
     * Удаляет все существующие карты, связанные с сессией, создает новую колоду,
     * перемешивает ее и сохраняет в базе данных.
     *
     * @param gameSession игровая сессия, для которой инициализируется колода
     */
    @Transactional
    public void initializeDeck(GameSession gameSession) {
        cardRepository.deleteAllByGameSession(gameSession);

        List<Card> deck = new ArrayList<>();

        deck.add(createCard("Block", CardType.ACTION, 1, gameSession));
        deck.add(createCard("Steal", CardType.ACTION, 3, gameSession));
        deck.add(createCard("Double Down", CardType.ACTION, 2, gameSession));
        deck.add(createCard("Small Points", CardType.POINTS, 2, gameSession));
        deck.add(createCard("Medium Points", CardType.POINTS, 7, gameSession));
        deck.add(createCard("Mega Points", CardType.POINTS, 10, gameSession));

        Collections.shuffle(deck);

        for(int i = 0; i < deck.size(); i++) {
            deck.get(i).setOrderIndex(i);
        }

        List<Card> savedCards = cardRepository.saveAll(deck);


        gameSession.getDeck().clear();
        gameSession.getDeck().addAll(savedCards);
    }
    /**
     * Создает новую карту с указанными параметрами.
     *
     * @param name название карты
     * @param type тип карты ({@link CardType#ACTION} или {@link CardType#POINTS})
     * @param value числовое значение карты
     * @param gameSession игровая сессия, к которой привязана карта
     * @return созданный объект карты
     */
    private Card createCard(String name, CardType type, int value, GameSession gameSession) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setValue(value);
        card.setGameSession(gameSession);
        card.setPlayed(false);
        card.setOrderIndex(0);
        return card;
    }
    /**
     * Берет следующую карту из колоды.
     * <p>
     * Если доступных карт нет, автоматически перетасовывает сброшенные карты.
     *
     * @param gameSession игровая сессия
     * @return карта из верхушки колоды
     * @throws GameException если в колоде нет доступных карт даже после перетасовки
     */
    @Transactional
    public Card drawCard(GameSession gameSession) {
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

    Card card = availableCards.get(0);
    card.setPlayed(true);
    cardRepository.save(card);

    return card;
}
    /**
     * Перемешивает сброшенные карты и возвращает их в колоду.
     * <p>
     * Сбрасывает флаг `played` у всех карт и обновляет их порядок.
     *
     * @param gameSession игровая сессия
     */
    private void reshuffleDiscardedCards(GameSession gameSession) {
        List<Card> deck = gameSession.getDeck();
        Collections.shuffle(deck);


        for (int i = 0; i < deck.size(); i++) {
            deck.get(i).setOrderIndex(i);
            deck.get(i).setPlayed(false);
        }

        cardRepository.saveAll(deck);
        log.info("Deck reshuffled for game {}", gameSession.getId());
    }
    /**
     * Применяет эффект карты и создает запись о ходе.
     * <p>
     * В зависимости от типа карты:
     * <ul>
     *   <li>Для {@link CardType#POINTS} - начисляет очки</li>
     *   <li>Для {@link CardType#ACTION} - применяет специальный эффект:
     *     <ul>
     *       <li>"Block" - блокирует следующего игрока</li>
     *       <li>"Steal" - крадет очки у соперника</li>
     *       <li>"Double Down" - удваивает текущие очки</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @param gameSession игровая сессия
     * @param card карта для розыгрыша
     * @param user игрок, разыгрывающий карту
     * @return DTO с информацией о ходе
     * @throws GameException если указана неизвестная карта
     */
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
            action = String.format("Игрок %s получил %d очков",
                    user.getName(), card.getValue());
        } else {
            switch (card.getName()) {
                case "Block":
                    // Block Card: value=1, следующий игрок пропускает ход
                    gameSession.setBlockNextPlayer(true);
                    action = String.format("Игрок %s блокирует следующего игрока",
                            user.getName());
                    break;

                case "Steal":
                    // Steal Card: value=N, кража N очков
                    User opponent = chooseRandomOpponent(gameSession, user);
                    int opponentScore = userService.getUserScore(opponent, gameSession);
                    int stealValue = card.getValue();
                    int stolenPoints = Math.min(stealValue, opponentScore);

                    userService.addScore(opponent, gameSession, -stolenPoints);
                    scoreAfter = userService.addScore(user, gameSession, stolenPoints);
                    action = String.format("Игрок %s украл %d очков у %s",
                            user.getName(), stolenPoints, opponent.getName());
                    break;

                case "Double Down":
                    // DoubleDown Card: value=2, удвоение очков (макс 30)
                    int currentScore = userService.getUserScore(user, gameSession);
                    int maxPossible = 30 - currentScore;
                    int pointsToAdd = Math.min(currentScore, maxPossible);

                    scoreAfter = userService.addScore(user, gameSession, pointsToAdd);
                    action = String.format("Игрок %s удвоил очки: %d → %d",
                            user.getName(), scoreBefore, scoreAfter);
                    break;

                default:
                    throw new GameException("Неизвестная карта: " + card.getName());
            }
        }

        turn.setAction(action);
        turn.setScoreBefore(scoreBefore);
        turn.setScoreAfter(scoreAfter);
        turnRepository.save(turn);

        return turnMapper.toDto(turn);
    }
    /**
     * Выбирает случайного оппонента для карты "Steal".
     *
     * @param gameSession игровая сессия
     * @param currentUser текущий игрок
     * @return случайно выбранный оппонент
     * @throws GameException если нет доступных оппонентов
     */
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

