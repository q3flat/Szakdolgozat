package hu.elte.bfw1p6.poker.client.model;

import java.math.BigDecimal;
import java.rmi.RemoteException;

import hu.elte.bfw1p6.poker.client.controller.main.CommunicatorController;
import hu.elte.bfw1p6.poker.command.PlayerPokerCommand;
import hu.elte.bfw1p6.poker.command.holdem.HoldemHousePokerCommand;
import hu.elte.bfw1p6.poker.command.holdem.HoldemPlayerPokerCommand;
import hu.elte.bfw1p6.poker.command.type.HoldemHousePokerCommandType;
import hu.elte.bfw1p6.poker.command.type.HoldemPlayerPokerCommandType;
import hu.elte.bfw1p6.poker.command.type.api.PokerCommandType;
import hu.elte.bfw1p6.poker.exception.PokerDataBaseException;
import hu.elte.bfw1p6.poker.exception.PokerUnauthenticatedException;
import hu.elte.bfw1p6.poker.exception.PokerUserBalanceException;

public class HouseClientMainGameModel extends AbstractPokerClientModel<HoldemHousePokerCommandType, HoldemHousePokerCommand, HoldemPlayerPokerCommandType, HoldemPlayerPokerCommand> {

	public HouseClientMainGameModel(CommunicatorController communicatorController) {
		super(communicatorController);
	}

	@Override
	public void sendCommandToTable(PlayerPokerCommand<HoldemPlayerPokerCommandType> playerPokerCommand) throws RemoteException, PokerUnauthenticatedException, PokerDataBaseException, PokerUserBalanceException {
		playerPokerCommand.setSender(pokerSession.getPlayer().getUserName());
		pokerRemote.sendPlayerCommand(pokerSession.getId(), pokerTable, communicatorController, playerPokerCommand);
		//		System.out.println("uj balance: " + pokerSession.getPlayer().getBalance());
	}

	/**
	 * Ha BLIND utasítás jött a szervertől
	 * @param houseHoldemCommand a szerver utasítás
	 * @throws PokerUserBalanceException 
	 * @throws PokerDataBaseException 
	 * @throws PokerUnauthenticatedException 
	 */
	public void receivedBlindHouseCommand(HoldemHousePokerCommand houseHoldemCommand) throws PokerUnauthenticatedException, PokerDataBaseException, PokerUserBalanceException {
		myDebt = pokerTable.getDefaultPot();
		youAreNth = houseHoldemCommand.getNthPlayer();
		players = houseHoldemCommand.getPlayers();
		// első körben az a dealer, aki elsőként csatlakozott, roundonként +1
		//		System.out.println("Hanyadik játékos vagy a szerveren: " + youAreNth);
		//		System.out.println("Aktuális dealer: " + houseHoldemCommand.getDealer());
		if (areYouTheSmallBlind(houseHoldemCommand)) {
			//			System.out.println("Betettem a kis vakot");
			tossBlind(false);
		} else if (areYouTheBigBlind(houseHoldemCommand)) {
			//			System.out.println("Betettem a nagy vakot");
			tossBlind(true);
		}
		// nagyvaktól eggyel balra ülő kezd
		//		System.out.println("Az éppen soron levő játékos: " + houseHoldemCommand.getWhosOn());
	}

	/**
	 * A vakot rakja be az asztalra.
	 * @param bigBlind ha nagy vakot szeretnénk berakni, akkor az értéke true, különben false
	 * @throws PokerUserBalanceException 
	 * @throws PokerDataBaseException 
	 * @throws PokerUnauthenticatedException 
	 */
	private void tossBlind(Boolean bigBlind) throws PokerUnauthenticatedException, PokerDataBaseException, PokerUserBalanceException {
		BigDecimal amount = pokerTable.getDefaultPot().divide(new BigDecimal(bigBlind ? 1 : 2));
		myDebt = myDebt.subtract(amount);
		sendPlayerCommand(HoldemPlayerPokerCommandType.BLIND, amount, null, -1);
	}


	/**
	 * A dealer mellett közvetlenül balra ülő játékos köteles kis vakot betenni.
	 * @return ha nekem kell betenni a kis vakot, akkor true, különben false.
	 */
	private boolean areYouTheSmallBlind(HoldemHousePokerCommand houseHoldemCommand) {
		return youAreNth == ((houseHoldemCommand.getDealer() + 1) % players);
	}

	/**
	 * A dealer mellett kettővel balra ülő játékos köteles nagy vakot betenni.
	 * @return ha nekem kell betenni a nagy vakot, akkor true, különben false.
	 */
	private boolean areYouTheBigBlind(HoldemHousePokerCommand houseHoldemCommand) {
		return youAreNth == ((houseHoldemCommand.getDealer() + 2) % players);
	}

	public void call() throws PokerUnauthenticatedException, PokerDataBaseException, PokerUserBalanceException {
		BigDecimal amount = BigDecimal.ZERO.add(myDebt);
		myDebt = myDebt.subtract(amount);
		sendPlayerCommand(HoldemPlayerPokerCommandType.CALL, amount, null, -1);
	}

	public void check() throws PokerUnauthenticatedException, PokerDataBaseException, PokerUserBalanceException {
		sendPlayerCommand(HoldemPlayerPokerCommandType.CHECK, null, null, -1);
	}

	public void raise(BigDecimal amount) throws PokerUnauthenticatedException, PokerDataBaseException, PokerUserBalanceException {
		sendPlayerCommand(HoldemPlayerPokerCommandType.RAISE, myDebt, amount, -1);
	}

	public void fold() throws PokerUnauthenticatedException, PokerDataBaseException, PokerUserBalanceException {
		int tempNth = youAreNth;
		youAreNth = -1;
		sendPlayerCommand(HoldemPlayerPokerCommandType.FOLD, null, null, tempNth);
	}

	public void quit() throws PokerUnauthenticatedException, PokerDataBaseException, PokerUserBalanceException {
		sendPlayerCommand(HoldemPlayerPokerCommandType.QUIT, null, null, youAreNth);		
	}

	public void receivedFoldPlayerCommand(HoldemPlayerPokerCommand playerHoldemCommand) {
		if (youAreNth > playerHoldemCommand.getWhosQuit()) {
			--youAreNth;
		}
	}

	public void receivedRaisePlayerCommand(HoldemPlayerPokerCommand playerHoldemCommand) {
		// és mi van ha én magam emeltem...
		// ha én magam emeltem, akkor a szerver elszámolta a teljes adósságom
		myDebt = playerHoldemCommand.getSender().equals(getUserName()) ? BigDecimal.ZERO : myDebt.add(playerHoldemCommand.getRaiseAmount());
	}


	private void sendPlayerCommand(PokerCommandType<HoldemPlayerPokerCommandType> type, BigDecimal callAmount, BigDecimal raiseAmount, Integer whosQuit) throws PokerUnauthenticatedException, PokerDataBaseException, PokerUserBalanceException {
		PlayerPokerCommand<HoldemPlayerPokerCommandType> playerPokerCommand = new PlayerPokerCommand<>(callAmount, raiseAmount, whosQuit);
		playerPokerCommand.setType(HoldemPlayerPokerCommandType.BLIND);
		try {
			sendCommandToTable(playerPokerCommand);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*new Thread() {

			@Override
			public void run() {
				try {
					PlayerHoldemCommand playerHoldemCommand = new PlayerHoldemCommand(type, callAmount, raiseAmount, whosQuit);
					sendCommandToTable(communicatorController, playerHoldemCommand);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (PokerUnauthenticatedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (PokerDataBaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (PokerUserBalanceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}}.start();*/
	}


	public void receivedBlindPlayerCommand(HoldemPlayerPokerCommand playerHoldemCommand) {
		// TODO Auto-generated method stub

	}

	public void receivedCallPlayerCommand(HoldemPlayerPokerCommand playerHoldemCommand) {
		// TODO Auto-generated method stub

	}

	public void receivedCheckPlayerCommand(HoldemPlayerPokerCommand playerHoldemCommand) {
		// TODO Auto-generated method stub

	}

	public void receivedPlayerHouseCommand(HoldemHousePokerCommand houseHoldemCommand) {
		pokerSession.getPlayer().setCards(houseHoldemCommand.getCards());
	}
}
