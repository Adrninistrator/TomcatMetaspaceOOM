package com.test.javaagent;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author adrninistrator
 * @date 2024/12/13
 * @description:
 */
public interface AgentDataInterface extends Remote {
    int getClassLoadTimes(String className) throws RemoteException;

    String getAllClassLoadTimes() throws RemoteException;
}
