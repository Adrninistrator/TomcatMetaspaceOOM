package com.test.javaagent;

import java.lang.instrument.Instrumentation;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * @author adrninistrator
 * @date 2024/12/12
 * @description:
 */
public class ClassLoadAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        try {
            System.out.println("### " + ClassLoadAgent.class.getName() + " classLoader " + ClassLoadAgent.class.getClassLoader());

            AgentDataImpl agentData = new AgentDataImpl();

            LocateRegistry.createRegistry(JavaAgentConstants.RMI_PORT);
            Naming.bind(JavaAgentConstants.RMI_URI, agentData);

            MyClassFileTransformer classFileTransformer = new MyClassFileTransformer(agentData);
            inst.addTransformer(classFileTransformer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
