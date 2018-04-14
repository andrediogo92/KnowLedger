package pt.um.lei.masb.agent;
import jade.core.Runtime;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import pt.um.lei.masb.blockchain.BlockChain;

public class Container {

  Runtime rt;
  ContainerController container;

  public ContainerController initContainerInPlatform(String host, String port, String containerName) {
    // Get the JADE runtime interface (singleton)
    this.rt = Runtime.instance();

    // Create a Profile, where the launch arguments are stored
    var profile = new ProfileImpl();
    profile.setParameter(Profile.CONTAINER_NAME, containerName);
    profile.setParameter(Profile.MAIN_HOST, host);
    profile.setParameter(Profile.MAIN_PORT, port);
    // create a non-main agent container
    ContainerController container = rt.createAgentContainer(profile);
    return container;
  }

  public void initMainContainerInPlatform(String host, String port, String containerName) {

    // Get the JADE runtime interface (singleton)
    this.rt = Runtime.instance();

    // Create a Profile, where the launch arguments are stored
    var prof = new ProfileImpl();
    prof.setParameter(Profile.CONTAINER_NAME, containerName);
    prof.setParameter(Profile.MAIN_HOST, host);
    prof.setParameter(Profile.MAIN_PORT, port);
    prof.setParameter(Profile.MAIN, "true");
    prof.setParameter(Profile.GUI, "true");

    // create a main agent container
    this.container = rt.createMainContainer(prof);
    rt.setCloseVM(true);

  }

  public void startAEInPlatform(String name, String classpath,BlockChain bc) {

    try {
      var ac = container.createNewAgent(name, classpath, new Object[]{bc});
      ac.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws InterruptedException {

    var a = new Container();
    var bc=new BlockChain();

    a.initMainContainerInPlatform("localhost", "9888", "Container");

    a.startAEInPlatform("MinerAgent", "pt.um.lei.masb.agent.agentZero",bc);

  }



}