package hu.elte.bfw1p6.poker.client.repository;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.UUID;

import hu.elte.bfw1p6.poker.properties.PokerProperties;
import hu.elte.bfw1p6.poker.rmi.PokerRemote;

public class RMIRepository {
	private static RMIRepository instance = null;

	//	private Registry registry;
	private PokerRemote pokerRemote;
	private UUID sessionId;

	private final String SVNAME;
	private final String PORT;

	private PokerProperties pokerProperties;

	private RMIRepository() {
		pokerProperties = PokerProperties.getInstance();
		SVNAME =  pokerProperties.getProperty("name");
		PORT = pokerProperties.getProperty("rmiport");
		try {
			System.out.println(pokerProperties.getProperty("rmiport"));
			System.out.println(pokerProperties.getProperty("name"));
			//			registry = LocateRegistry.getRegistry(Integer.valueOf(pokerProperties.getProperty("rmiport")));
			//			pokerRemote = (PokerRemote) registry.lookup(pokerProperties.getProperty("name"));

			pokerRemote = (PokerRemote) Naming.lookup("//localhost:" + PORT + "/" + SVNAME);

		} catch (RemoteException e) {
			System.out.println("lol");
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static RMIRepository getInstance() {
		if(instance == null) {
			instance = new RMIRepository();
		}
		return instance;
	}

	public PokerRemote getPokerRemote() {
		return pokerRemote;
	}

	public void setSessionId(UUID sessionId) {
		this.sessionId = sessionId;
	}

	public UUID getSessionId() {
		return sessionId;
	}
}
