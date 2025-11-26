package sianc.sisar.agent;

import java.sql.Connection;
import java.util.Date;

import PSGExt.agent.AgentInterface;
import PSGExt.agent.monitor.AgentContext;
import PSGExt.agent.monitor.MonitorableAgentInterface;
import PSGExt.services.BinderInterface;
import PSGExt.services.ServiceContext;
import PSGExt.services.ServiceManager;
import PSGLibrary.UserInterface;
import PSGLibrary.DB.ConnectionManager;
import PSGLibrary.DB.SequenceManager;
import sianc.sisar.corso.Corso;

public class SisarAgentMC implements AgentInterface, MonitorableAgentInterface {

  private Long idAgent;
  private UserInterface ui;
  private AgentContext agentContext;

  @Override
  public void run() {
    // business logic dell'agent

    Connection conn = null;
    String codiceCorso = null;
    try {

      agentContext.getLogger().logInfo("inizio metodo run() dell'agent MC SisarAgent " );
      conn = ConnectionManager.getConnection(ui, 999);
      ServiceContext context = new ServiceContext(ui);
      context.setConnection(conn);
      //recupero una nuova istanza di corso e ne valorizzo descrizone, codice e data inizio
      BinderInterface binderCorso = ServiceManager.getObject(Corso.class.getName(), null, context);
      binderCorso.setPropertyValue("description", "corso generato da agent", context);
      binderCorso.setPropertyValue("startDate", new Date(), context);

      codiceCorso = "AGENT_" + SequenceManager.getNewValue(conn, "SEQ_SARD_CORSO_WIZARD");
      binderCorso.setPropertyValue("code", codiceCorso, context);

      agentContext.getLogger().logDebug("Sto per salvare in DB il corso con codice = " + codiceCorso);
      //salvo il corso generato e committo
      ServiceManager.save(binderCorso, context);
      conn.commit();

    } catch (Exception e) {
      agentContext.getLogger().logError("Si e' verificato un errore in fase di salvataggio corso da agent con codice corso = " + codiceCorso);
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
    finally {
      ConnectionManager.releaseConnection(conn);
      agentContext.getLogger().logDebug("Agent salvataggio corso con codice = " + codiceCorso + " : RELEASE CONNECTION ESEGUITA CON SUCCESSO");
    }

    agentContext.getLogger().logInfo("fine metodo run() dell'agent MC SisarAgent " );
  }

  @Override
  public void setIdAgent(Long idAgent) {
    // serve per scrivere i log ad esempio
    this.idAgent = idAgent;


  }

  @Override
  public void setUserInterface(UserInterface ui) {
    //UserInterface: mi ci viene passato l'utente di profilatura del demone
    // che fa da container ed ospita il mio agent : serve ad esempio per prendermi una connessione
    this.ui = ui;
  }

  @Override
  public void setAgentContext(AgentContext context) {
    this.agentContext = context;
  }

}
