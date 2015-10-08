package hu.elte.bfw1p6.poker.rmi;

import java.io.Serializable;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import hu.elte.bfw1p6.poker.client.observer.controller.PokerRemoteObserverTableViewController;
import hu.elte.bfw1p6.poker.model.entity.PTable;
import hu.elte.bfw1p6.poker.model.entity.Player;
import hu.elte.bfw1p6.poker.model.entity.PokerType;
import hu.elte.bfw1p6.poker.persist.ptable.PTableRepository;

public class PokerRemoteImpl implements PokerRemote, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private HashMap<UUID, PokerRemoteObserverTableViewController> observers;
	

	public PokerRemoteImpl() {
		observers = new HashMap<>();
	}

	@Override
	public void deleteUser(int id) {
	}

	@Override
	public String sayHello() {
		return "hello";
	}

	@Override
	public void deleteTable(int id) throws RemoteException {
	}

	@Override
	public void createTable(PTable t) throws RemoteException {
		PTableRepository.save(t);
		System.out.println("létrehozta");
	}

	@Override
	public void modifyTable(PTable t) throws RemoteException {
	}

	@Override
	public void modifyUser(Player player) throws RemoteException {
	}

	@Override
	public void modifyPassword(String username, String oldPassword, String newPassword) throws RemoteException {
	}

	@Override
	public List<PTable> getTables() throws RemoteException {
		return PTableRepository.findAll();
//		List<PTable> tables = new ArrayList<>();
//		PTable table = new PTable("szerver", 23, 4, new BigDecimal(213), new BigDecimal(100), new BigDecimal(200), PokerType.HOLDEM);
//		tables.add(table);
//		return tables;
	}

	@Override
	public void registerObserver(UUID uuid, PokerRemoteObserverTableViewController proc) throws RemoteException {
		observers.put(uuid, proc);
		proc.updateTableView(getTables());
	}

	@Override
	public void unRegisterObserver(UUID uuid, PokerRemoteObserverTableViewController pcc) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
	
}
