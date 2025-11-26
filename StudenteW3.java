package sianc.sisar.studente;

import javax.servlet.http.HttpServletRequest;

import PSGLibrary.w3.controller.W3ControllerPersistent;
import PSGLibrary.w3.util.ServiceResultBuilder;


public class StudenteW3 extends W3ControllerPersistent {



  @Override
  public void init(HttpServletRequest req) throws Exception {
    super.init(req);
    // Init the Query class
    // primo parametro : classe query associata al bean = CorsoQuery.class
    // secondo parametro : stringa con il nome della proprietà sul bean che è la PK della tabella in DB.
    // terzo parametro: user - utente di connessione corrente.
    ServiceResultBuilder serviceResultBuilder = new ServiceResultBuilder(StudenteQuery.class.getName(), "id", user);
    serviceResultBuilder.setActionPath(getControllerName() + ".do?ACTION=EDIT");
    serviceResultBuilder.setPaginable(true);
    setFindResultBuilder(serviceResultBuilder);

    // Init the Views
    setReturnPage(STATUS_FIND, "/sianc/sisar/studente/studenteFind.jsp");
    setReturnPage(STATUS_EDIT, "/sianc/sisar/studente/studenteEdit.jsp");
    setReturnPage(STATUS_INSERT, STATUS_EDIT);
    setReturnPage(STATUS_VIEW, STATUS_EDIT);

    // Init the first view to show
    setStatus(STATUS_FIND);
    setLastPageForward(getReturnPage(getStatus()));

    useService = true;
    // come parametro gli devo passare la classe del bean
    enableProfiler(Studente.class.getClass());

  }

}
