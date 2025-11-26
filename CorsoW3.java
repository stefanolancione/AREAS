package sianc.sisar.corso;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import PSGLibrary.w3.ControllerInterface;
import PSGLibrary.w3.controller.W3ControllerPersistent;
import PSGLibrary.w3.forward.ControllerForward;
import PSGLibrary.w3.util.ButtonExecutorException;
import PSGLibrary.w3.util.ButtonExecutorInterface;
import PSGLibrary.w3.util.ButtonInterface;
import PSGLibrary.w3.util.ServiceResultBuilder;
import PSGLibrary.w3.util.Tab;

public class CorsoW3 extends W3ControllerPersistent {

  //property aggiunta per supporto FIND3 in form di ricerca corso : selezione multipla studenti
  private List<Object> studentiSelezionati = new ArrayList<Object>();

  @Override
  public void init(HttpServletRequest req) throws Exception {
    super.init(req);
    // Init the Query class
    // primo parametro : classe query associata al bean = CorsoQuery.class
    // secondo parametro : stringa con il nome della proprietà sul bean che è la PK della tabella in DB.
    // terzo parametro: user - utente di connessione corrente.
    ServiceResultBuilder serviceResultBuilder = new ServiceResultBuilder(CorsoQuery.class.getName(), "code", user);
    serviceResultBuilder.setActionPath(getControllerName() + ".do?ACTION=EDIT");
    serviceResultBuilder.setPaginable(true);
    setFindResultBuilder(serviceResultBuilder);

    // Init the Views
    setReturnPage(STATUS_FIND, "/sianc/sisar/corso/corsoFind.jsp");
    setReturnPage(STATUS_EDIT, "/sianc/sisar/corso/corsoEdit.jsp");
    setReturnPage(STATUS_INSERT, STATUS_EDIT);
    setReturnPage(STATUS_VIEW, STATUS_EDIT);

    // Init the first view to show
    setStatus(STATUS_FIND);
    setLastPageForward(getReturnPage(getStatus()));

    useService = true;
    // come parametro gli devo passare la classe del bean
    enableProfiler(Corso.class.getClass());

    // **** GESTIONE TAB NEL CASO DI PRESENZA DETTAGLI *****
    // Init the tabs : main + dettagli (esami e studenti)
    Tab tab = getTabsList().addTab("TAB_CORSO", "Corso");
    tab.setAction(ACTION_TAB);

    tab = getTabsList().addTab("TAB_ESAMI", "Esami");
    tab.setAction(ACTION_TAB);

    tab.setExecutor(new ButtonExecutorInterface() {
      public Object execute(ControllerInterface controller, ButtonInterface button, HttpServletRequest req, String action)
          throws ButtonExecutorException {
        // adeguare con il mapping del controller di dettaglio e con l'istanza del controller padre
        return new ControllerForward(req, "/sisar.esame.do?ACTION=INSERT", CorsoW3.this);
      }
    });


    tab = getTabsList().addTab("TAB_STUDENTI", "Studenti");
    tab.setAction(ACTION_TAB);

    tab.setExecutor(new ButtonExecutorInterface() {
      public Object execute(ControllerInterface controller, ButtonInterface button, HttpServletRequest req,
          String action)
          throws ButtonExecutorException {
        return new ControllerForward(req, "/sisar.corso.studenti.do?ACTION=INSERT", CorsoW3.this);
      }
    });


    getTabsList().setActive("TAB_CORSO");

  }

  public List getCorsoStatoOptionsList() {
    //key/description comma separated
    String options = "T,Tutti,A,Attivi,D,Disattivi";
    return PSGLibrary.util.ListUtils.createCodeValueList(options);
  }


  //aggiunti per gestione FIND3: lista studenti selezionati da form ricerca conto.
  public List<Object> getStudentiSelezionati() {
    return studentiSelezionati;
  }

  public void setStudentiSelezionati(List<Object> studentiSelezionati) {
    this.studentiSelezionati = studentiSelezionati;
  }

  //gestione FIND3: override di doConfirmFind necessario per passare il filtro con la lista degli
  //studenti selezionati nel widget find3, dalla pagina di ricerca del corso alla classe di query per poter applicare il filtro
  @Override
  protected Object doConfirmFind(HttpServletRequest req) throws Exception {
     if (studentiSelezionati != null && studentiSelezionati.size()>0) {
      getFindFilter().put("listaStudenti", studentiSelezionati);
    } else {
      getFindFilter().remove("listaStudenti");
    }

     return super.doConfirmFind(req);
  }

  @Override
  protected Object doCancelFind(HttpServletRequest req) throws Exception {
    getFindFilter().remove("listaStudenti");
    setStudentiSelezionati(new ArrayList<Object>());
    return super.doCancelFind(req);
  }



}
