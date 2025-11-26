package sianc.sisar.corso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import PSGLibrary.w3.ControllerContext;
import PSGLibrary.w3.ControllerStatusInterface;
import PSGLibrary.w3.actionHandler.ControllerForwardDecoratorInterface;
import PSGLibrary.w3.actionHandler.Find2ActionHandler;
import PSGLibrary.w3.controller.W3ControllerPersistentDetail;


public class CorsoStudenteW3 extends W3ControllerPersistentDetail {

  @Override
  // classe : W3ControllerPersistentDetail
  public void init(HttpServletRequest req) throws Exception {
    super.init(req);

    // Link to parent controller
    linkToParent();
    linkToParentDetail("studenti"); // The name of the property in the parent Bean
    linkTabsFromController(getParentController());

    // Init the Views
    setReturnPage(ControllerStatusInterface.STATUS_EDIT, "/sianc/sisar/corso/corsoStudenteDetail.jsp");
    setReturnPage(ControllerStatusInterface.STATUS_INSERT, STATUS_EDIT);
    setReturnPage(ControllerStatusInterface.STATUS_VIEW, STATUS_EDIT);
    setLastPageForward(getReturnPage(getStatus()));
    setStatus(STATUS_INSERT);

    // Controller settings
    useService = true;
    enableProfiler(CorsoStudente.class.getName());

    //gestione introduzione FIND2 su studente nella form di associazione corso-studente
    Find2ActionHandler find2Handler = (Find2ActionHandler) actionBinder.getActionHandlers().get(ACTION_FIND2);

    find2Handler.setFilterDecorator(new ControllerForwardDecoratorInterface() {


      // AGGIUNTO PER ESERCIZIO 5.3 : GESTIONE APERTURA FIND2 DA LISTAVALORI STUDENTE
      // actionHandler: intercetta le azioni che l'utente compie sulla maschera. in questo caso
      // voglio intercettare l'azione di tipo FIND2
      // prima di fasre il forward all'anagrafica dello studente, se il forward lo stai facendo a sisar.studente
      // (coincide con l'attributo che ho messo sul bottone della popup lista valori in pagina :
      //          <psg:lookupProperty propertyNames="studente" linkedController="sisar.studente">
      //
      // allora passa alla popup chiamata (ricerca studente) passa anche i seguenti filtri che ti aggiungo io.
      // in questo caso : devo vedere i filtri che prevede la maschera di ricerca degli studenti
      // (StudenteQuery.class)
      public void beforeControllerForward(String controllerForward, ControllerContext context, HttpServletRequest req, String options, Map originalReqParams) {
        if ("sisar.studente".equals(controllerForward)) {
          Map filters = new HashMap();
          //devo far riferimnento ai filtri previsti dalla form di ricerca su cui sto dirottando la popup della lista valori con la find2.
          //Questo filtro arriva alla classe Query del bean associato alla pagina di ricerca aperta come popup della FIND2!!!
          //il filtro che imposto qui, se non gia presente, andra aggiunto alla queryClass del bean "referenziato"!!!
          filters.put("soloIscritti", new Date());

          context.set(CONTEXT_FIND_FILTERS, filters); // aggiunge il filtro al contesto
        }
      }
    });

  }

}
