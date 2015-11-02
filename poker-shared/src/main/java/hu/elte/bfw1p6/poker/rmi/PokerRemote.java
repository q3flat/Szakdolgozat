package hu.elte.bfw1p6.poker.rmi;

import java.io.Serializable;
import java.math.BigDecimal;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

import hu.elte.bfw1p6.poker.client.observer.RemoteObserver;
import hu.elte.bfw1p6.poker.client.observer.TableViewObserver;
import hu.elte.bfw1p6.poker.command.PlayerCommand;
import hu.elte.bfw1p6.poker.command.type.HoldemPlayerPokerCommandType;
import hu.elte.bfw1p6.poker.command.type.api.PokerCommandType;
import hu.elte.bfw1p6.poker.exception.PokerDataBaseException;
import hu.elte.bfw1p6.poker.exception.PokerInvalidPassword;
import hu.elte.bfw1p6.poker.exception.PokerInvalidUserException;
import hu.elte.bfw1p6.poker.exception.PokerTooMuchPlayerException;
import hu.elte.bfw1p6.poker.exception.PokerUnauthenticatedException;
import hu.elte.bfw1p6.poker.exception.PokerUserBalanceException;
import hu.elte.bfw1p6.poker.model.PokerSession;
import hu.elte.bfw1p6.poker.model.entity.PokerPlayer;
import hu.elte.bfw1p6.poker.model.entity.PokerTable;

public interface PokerRemote extends Remote, Serializable {

	/**
	 * Regisztráció a póker játékba
	 * @param username a felhasználói név
	 * @param password a hozzáférés jelszava
	 * @throws RemoteException
	 * @throws PokerDataBaseException
	 */
	void registration(String username, String password) throws RemoteException, PokerDataBaseException;
	
	/**
	 * Felhasználói hozzáférés törlése
	 * @param a kliens egyedi session azonosítója
	 * @param u a User, akit ki szeretnénk törölni
	 * @throws RemoteException
	 * @throws PokerDataBaseException
	 */
	void deletePlayer(UUID uuid, PokerPlayer player) throws RemoteException, PokerDataBaseException;

	/**
	 * Adott felhasználói fiókhoz tartozó jelszó cseréje
	 * @param a kliens egyedi session azonosítója
	 * @param username a felhasználói név
	 * @param oldPassword a régi jelszó
	 * @param newPassword az új jelszó
	 * @throws RemoteException
	 */
	void modifyPassword(UUID uuid, String oldPassword, String newPassword) throws RemoteException, PokerDataBaseException, PokerInvalidPassword, PokerUnauthenticatedException;
	
	BigDecimal refreshBalance(UUID uuid) throws RemoteException, PokerDataBaseException, PokerUnauthenticatedException;
	
	
	
	
	/**
	 * Új játéktáblát hoz létre
	 * @param a kliens egyedi session azonosítója
	 * @param t a létrehozandó tábla entitás
	 * @throws RemoteException
	 * @throws PokerDataBaseException
	 */
	void createTable(UUID uuid, PokerTable t) throws RemoteException, PokerDataBaseException, PokerUnauthenticatedException;
	
	/**
	 * Már meglévő játéktáblát töröl
	 * @param a kliens egyedi session azonosítója
	 * @param pokerTable a törlendő játéktábla
	 * @throws RemoteException
	 * @throws PokerDataBaseException
	 * @throws PokerUnauthenticatedException 
	 */
	void deleteTable(UUID uuid, PokerTable pokerTable) throws RemoteException, PokerDataBaseException, PokerUnauthenticatedException;

	/**
	 * Már meglévő játéktábla módosítása
	 * @param a kliens egyedi session azonosítója
	 * @param t a módosítandó játéktábla
	 * @throws RemoteException
	 * @throws PokerDataBaseException
	 * @throws PokerUnauthenticatedException 
	 */
	void modifyTable(UUID uuid, PokerTable t) throws RemoteException, PokerDataBaseException, PokerUnauthenticatedException;

	/**
	 * Az összes játéktábla lekérése
	 * @param a kliens egyedi session azonosítója
	 * @return az összes játéktábla
	 * @throws RemoteException
	 * @throws PokerDataBaseException
	 * @throws PokerUnauthenticatedException 
	 */
	List<PokerTable> getTables(UUID uuid) throws RemoteException, PokerDataBaseException, PokerUnauthenticatedException;
	
	
	void unRegisterObserver(UUID uuid, TableViewObserver proc) throws RemoteException;




	/**
	 * Bejelentkezés a póker játékba
	 * @param username a felhasználónév
	 * @param password a jelszó
	 * @param username
	 * @param password
	 * @return PokerSession a kliensek számára egyedi UUID-val
	 * @throws RemoteException
	 * @throws PokerInvalidUserException
	 * @throws PokerDataBaseException
	 */
	PokerSession login(String username, String password) throws RemoteException, PokerInvalidUserException, PokerDataBaseException;

	/**
	 * Kijelentkezés a póker játékból
	 * @param a kliens egyedi session azonosítója
	 * @throws RemoteException
	 */
	void logout(UUID uuid) throws RemoteException;

	boolean shutDown(UUID uuid) throws RemoteException, PokerInvalidUserException;

	boolean isAdmin(UUID uuid) throws RemoteException, PokerUnauthenticatedException, PokerDataBaseException;

	List<PokerTable> registerTableViewObserver(UUID uuid, RemoteObserver observer) throws RemoteException, PokerDataBaseException, PokerUnauthenticatedException;

	void removeTableViewObserver(RemoteObserver observer) throws RemoteException;
	
	void sendPlayerCommand(UUID uuid, PokerTable t, RemoteObserver client, PokerCommandType<?> playerCommand) throws RemoteException, PokerUnauthenticatedException, PokerDataBaseException, PokerUserBalanceException;
	
	void connectToTable(UUID uuid, PokerTable t, RemoteObserver observer) throws RemoteException, PokerTooMuchPlayerException, PokerUnauthenticatedException;
}
