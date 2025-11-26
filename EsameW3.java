package sianc.sisar.corso;

import javax.servlet.http.HttpServletRequest;

import PSGLibrary.w3.ControllerStatusInterface;
import PSGLibrary.w3.controller.W3ControllerPersistentDetail;

public class EsameW3 extends W3ControllerPersistentDetail {

  @Override
  public void init(HttpServletRequest req) throws Exception {
    super.init(req);

    // Link to parent controller
    linkToParent();

    // The name della proprietà del padre che contiene la lista dei dettagli
    linkToParentDetail("esami");
    linkTabsFromController(getParentController());

    // Init the Views  :vecchia versione condettaglio inserimento nuovo record sotto la griglia
    /*
    setReturnPage(ControllerStatusInterface.STATUS_EDIT, "/sianc/sisar/corso/esameDetail.jsp");
    setReturnPage(ControllerStatusInterface.STATUS_INSERT, STATUS_EDIT);
    setReturnPage(ControllerStatusInterface.STATUS_VIEW, STATUS_EDIT);
    setLastPageForward(getReturnPage(getStatus()));
    setStatus(STATUS_INSERT);
    */

    // UNICA MODIFICA NECESSRIA PER PASSARE DA INSERIMENTO DETTAGLIO STANDARD A MODALITA IN GRIGLIA
    // puntare alla nuova pagina corsoDetailGrid.jsp
    // Init the Views  :STATUS_GRID E' PER LA VISUALIZZAZIONE IN MODALITA GRIGLIA MODIFICABILE!!!!
    setReturnPage(ControllerStatusInterface.STATUS_GRID, "/sianc/sisar/corso/esameDetailGrid.jsp");
    setReturnPage(ControllerStatusInterface.STATUS_INSERT, STATUS_GRID);
    setReturnPage(ControllerStatusInterface.STATUS_VIEW, STATUS_GRID);
    setStatus(STATUS_GRID);
    setLastPageForward(getReturnPage(getStatus()));

    // Controller settings
    useService = true;
    enableProfiler(Esame.class.getName());
  }

}
