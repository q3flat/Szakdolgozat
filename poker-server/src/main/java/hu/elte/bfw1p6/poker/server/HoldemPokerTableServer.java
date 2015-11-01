package hu.elte.bfw1p6.poker.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.cantero.games.poker.texasholdem.Card;
import com.cantero.games.poker.texasholdem.IPlayer;

import hu.elte.bfw1p6.poker.client.observer.RemoteObserver;
import hu.elte.bfw1p6.poker.command.PokerCommand;
import hu.elte.bfw1p6.poker.command.holdem.HoldemHouseCommand;
import hu.elte.bfw1p6.poker.command.holdem.HoldemPlayerCommand;
import hu.elte.bfw1p6.poker.command.type.HoldemHouseCommandType;
import hu.elte.bfw1p6.poker.exception.PokerDataBaseException;
import hu.elte.bfw1p6.poker.exception.PokerUserBalanceException;
import hu.elte.bfw1p6.poker.model.entity.PokerPlayer;
import hu.elte.bfw1p6.poker.model.entity.PokerTable;

/**
 * Maga a póker asztal megvalósítása
 * @author feher
 *
 */
public class HoldemPokerTableServer extends AbstractPokerTableServer {

	private static final long serialVersionUID = 2737496961750222946L;
	
	/**
	 * Épp milyen utasítást fog kiadni a szerver.
	 */
	private HoldemHouseCommandType actualHouseCommandType;

	/**
	 * Ház lapjai.
	 */
	private List<Card> houseCards;

	public HoldemPokerTableServer(PokerTable pokerTable) throws RemoteException {
		super(pokerTable);
		this.houseCards = new ArrayList<>();
	}

	@Override
	protected void startRound() {
		//ha elegen vagyunk az asztalnál, akkor indulhat a játék
		if (clients.size() >= minPlayer) {
			foldCounter = 0;
			//			a vakokat kérem be legelőször
			//			actualHoldemHouseCommandType = HoldemHouseCommandType.BLIND;
			actualHouseCommandType = HoldemHouseCommandType.values()[0];
			// megnézem, hogy aktuális hány játékos van az asztalnál
			playersInRound = clients.size();
			//következő játékos a dealer
			++dealer;
			//nem baj, ha körbeértünk...
			dealer %= playersInRound;
			//a kártyapaklit megkeverjük
			deck.reset();
			//senki sem beszélt még
			votedPlayers = 0;
			//a dealertől balra ülő harmadik játékos kezd
			whosOn = (dealer + 3) % playersInRound;
			//törlöm a ház lapjait
			houseCards.clear();
			//törlöm a játékosokat
			players.clear();
			//törlöm a játékosok lapjait
//			playersCards.clear();
			// be kell kérni a vakokat
			collectBlinds();
			// két lap kézbe
			dealCardsToPlayers();
		}
	}

	private void collectBlinds() {
		for (int i = 0; i < clients.size(); i++) {
			HoldemHouseCommand houseHoldemCommand = new HoldemHouseCommand();
			houseHoldemCommand.setUpBlindCommand(i, clients.size(), dealer, whosOn, clientsNames);
			sendPokerCommand(i, houseHoldemCommand);
		}
		nextStep();
	}

	private void sendPokerCommand(int i, PokerCommand pokerCommand) {
		System.out.println("spc, ertesitem a " + i + ". klienst!");
		if (pokerCommand instanceof HoldemHouseCommand) {
			System.out.println("Utasitas típusa: " + ((HoldemHouseCommand)pokerCommand).getHouseCommandType());
		} else if (pokerCommand instanceof HoldemPlayerCommand) {
			System.out.println("Utasitas típusa: " + ((HoldemPlayerCommand)pokerCommand).getPlayerCommandType());
		}
		System.out.println("-----------------------------------------------");
		new Thread() {

			@Override
			public void run() {
				try {
					clients.get(i).update(pokerCommand);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}}.start();
	}

	private void dealCardsToPlayers() {
		for (int i = 0; i < clients.size(); i++) {
			Card c1 = deck.popCard();
			Card c2 = deck.popCard();
			PokerPlayer pokerPlayer = new PokerPlayer();
			pokerPlayer.setCards(c1, c2);
			players.add(pokerPlayer);
			HoldemHouseCommand pokerCommand = new HoldemHouseCommand();
			pokerCommand.setUpPlayerCommand(c1, c2, whosOn);
			sendPokerCommand(i, pokerCommand);
		}
		nextStep();
	}

	private void notifyClients(PokerCommand pokerCommand) {
		//int i = 0;
		for (RemoteObserver pokerTableServerObserver : clients) {
			/*System.out.println("nc, ertesitem a " + i + ". klienst!");
			if (pokerCommand instanceof HoldemHouseCommand) {
				System.out.println("Utasitas típusa: " + ((HoldemHouseCommand)pokerCommand).getHouseCommandType());
			} else if (pokerCommand instanceof HoldemPlayerCommand) {
				System.out.println("Utasitas típusa: " + ((HoldemPlayerCommand)pokerCommand).getPlayerCommandType());
			}
			System.out.println("-----------------------------------------------");*/
			new Thread() {

				@Override
				public void run() {
					try {
						pokerTableServerObserver.update(pokerCommand);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}}.start();
				//++i;
		}
	}

	public synchronized void receivePlayerCommand(RemoteObserver client, HoldemPlayerCommand playerCommand) throws PokerDataBaseException, PokerUserBalanceException, RemoteException {
		// ha valid klienstől érkezik üzenet, azt feldolgozzuk, körbeküldjük
		if (clients.contains(client)) {
			switch(playerCommand.getPlayerCommandType()) {
			case BLIND: {
				refreshBalance(playerCommand);
				--whosOn;
				break;
			}
			case CALL: {
				refreshBalance(playerCommand);
				++votedPlayers;
				break;
			}
			case CHECK: {
				++votedPlayers;
				break;
			}
			case FOLD: {
				//				++votedPlayers;
				--playersInRound;
				players.remove(whosOn);
				--whosOn;
				++foldCounter;
				// mert aki nagyobb az ő sorszámánál, az lejjebb csúszik eggyel.
				break;
			}
			case RAISE: {
				refreshBalance(playerCommand);
				votedPlayers = 1;
				break;
			}
			case QUIT: {
				System.out.println("WhosQuit param: " + playerCommand.getWhosQuit());
				System.out.println("Kliens visszakeresve: " + clients.indexOf(client));
				clients.remove(client);
				//				++votedPlayers;
				--playersInRound;
				--whosOn;
				break;
			}
			default:
				break;
			}
			++whosOn;
			whosOn %= playersInRound;
			playerCommand.setWhosOn(whosOn);
			notifyClients(playerCommand);

			nextRound();
		}
	}

	@Override
	protected void nextRound() throws RemoteException {
		// ha már kijött a river és az utolsó körben (rivernél) már mindenki nyilatkozott legalább egyszer, akkor új játszma kezdődik
		System.out.println("VotedPlayers: " + votedPlayers);
		System.out.println("Players in round: " + playersInRound);
		if (playersInRound == 1 || (actualHouseCommandType == HoldemHouseCommandType.BLIND && votedPlayers >= playersInRound)) {
			//TODO: itt is kell értékelni, hogy ki nyert
			startRound();
		} else {
			// ha már mindenki nyilatkozott legalább egyszer (raise esetén újraindul a kör...)
			if (votedPlayers >= playersInRound) {
				HoldemHouseCommand houseHoldemCommand = new HoldemHouseCommand();
				// flopnál, turnnél, rivernél mindig a kisvak kezdi a gondolkodást! (persze kivétel, ha eldobta a lapjait, de akkor úgy is lecsúsznak a helyére
				whosOn = (dealer + 1 + foldCounter) % playersInRound;
				switch (actualHouseCommandType) {
				case FLOP: {
					houseCards.add(deck.popCard());
					houseCards.add(deck.popCard());
					houseCards.add(deck.popCard());
					houseHoldemCommand.setUpFlopCommand(houseCards.get(0), houseCards.get(1), houseCards.get(2), whosOn, foldCounter);
					break;
				}
				case TURN: {
					houseCards.add(deck.popCard());
					houseHoldemCommand.setUpTurnCommand(houseCards.get(3), whosOn, foldCounter);
					break;
				}
				case RIVER: {
					houseCards.add(deck.popCard());
					houseHoldemCommand.setUpRiverCommand(houseCards.get(4), whosOn, foldCounter);
					break;
				}
				case WINNER: {
					winner(houseHoldemCommand);
				}
				default:
					break;
				}
				System.out.println("Next round");
				notifyClients(houseHoldemCommand);
				nextStep();
				votedPlayers = 0;
			}
		}
	}

	private void winner(HoldemHouseCommand houseHoldemCommand) {
		List<IPlayer> winner = HoldemHandEvaluator.getInstance().getWinner(houseCards, players);
		Card[] cards = winner.get(0).getCards();
		// TODO: és mi van ha döntetlen?
		int winner_ = -1;
		System.out.println("Players size: " + players.size());
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getCards()[0].equals(cards[0]) && players.get(i).getCards()[1].equals(cards[1])) {
//				winner_ = (i - foldCounter) % playersInRound;
				winner_ = i;
				break;
			}
		}
		System.out.println("A győztes sorszáma: " + winner_);
		System.out.println("A győztes első lapja: " + cards[0]);
		System.out.println("A győztes második lapja: " + cards[1]);
		houseHoldemCommand.setUpWinnerCommand(cards[0], cards[1], winner_);
	}
	
	@Override
	public void nextStep() {
		actualHouseCommandType = actualHouseCommandType.getNext();
	}
}