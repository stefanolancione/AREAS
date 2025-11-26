package sianc.sisar.corso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import PSGLibrary.Persistence.JDBCMemento;
import PSGLibrary.Persistence.JDBCMementoDefs;
import PSGLibrary.Persistence.PersistenceException;
import PSGLibrary.Persistence.PersistenceHandler;
import PSGLibrary.Persistence.Persistent;
import PSGLibrary.util.DynamicInfoInterface;
import PSGLibrary.util.DynamicInfoUtils;
import PSGLibrary.util.InteractiveInterface;
import PSGLibrary.util.ValidableInterface;
import PSGLibrary.util.ValidateException;

// l'interfaccia InteractiveInterface va aggiunta, implementando l'override
// del metodo public long getPropertyInfo(String propertyName, int info)
// nel caso in cui vogliamo implementare visibilita/obbligatorieta su uno o piu' campi
public class Corso implements Persistent, ValidableInterface, DynamicInfoInterface, InteractiveInterface {

  private String code;
  private String description;
  private String teacher;
  private Date startDate;
  private Date endDate;
  private String remote; // checkbox

  //GESTIONE DETTAGLI : ESAMI
  private List<Esame> esami = new ArrayList<>();
  private List<CorsoStudente> studenti = new ArrayList<>();

  protected transient static JDBCMementoDefs memDefs = new JDBCMementoDefs("SA_SARD_CORSO",
      new String[]{"CR_CODE"}, "CR_UT_INS", "CR_DT_INS", "CR_UT_VAR", "CR_DT_VAR");
  protected transient JDBCMemento mem = new JDBCMemento(memDefs);

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTeacher() {
    return teacher;
  }

  public void setTeacher(String teacher) {
    this.teacher = teacher;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public String getRemote() {
    return remote;
  }

  public void setRemote(String remote) {
    this.remote = remote;
  }

  //GESTIONE DETTAGLI : ESAMI
  public List<Esame> getEsami() {
    return esami;
  }

  public void setEsami(List<Esame> esami) {
    this.esami = esami;
  }

  public List<CorsoStudente> getStudenti() {
    return studenti;
  }

  public void setStudenti(List<CorsoStudente> studenti) {
    this.studenti = studenti;
  }

  protected static DynamicInfoInterface infoDelegate;

  @Override
  public Object getObjectInfo(String info, String propertyName) {
    if (infoDelegate == null) {
      infoDelegate = DynamicInfoUtils.getBeanDynamicInfo(Corso.class);
    }

    return infoDelegate.getObjectInfo(info, propertyName);
  }

  @Override
  public void Store(PersistenceHandler ph) throws PersistenceException {
    mem.clear();

    mem.loadFromBean(this);
    ph.Store(mem);
  }

  @Override
  public void Retrieve(PersistenceHandler ph) throws PersistenceException {
    mem.clear();

    mem.loadKeyFromBean(this);
    ph.Retrieve(mem);
    mem.saveToBean(this);

    //GESTIONE DETTAGLI : ESAMI - recuperando un corso recupero da db anche la lista di dettagli esami.
    // primo parametro : classname del tipo di dettaglio
    // penultimo parametro : property del bean padre da popolare con i dettagli recuperati (lista dettagli)
    // ultimo parametro : passare alla getReferenceForeignKey la stringa con il nome della colonna, nella tabella DETTAGLIO, che
    //                    contiene la FK verso il padre. in questo caso ES_CORSO di SA_SARD_ESAME
    ph.getPersistenceManager().Find(Esame.class.getName(), new Class[]{Object.class}, new Object[]{this},
        esami, mem.getReferenceForeignKey("ES_CORSO"));

    // dettagli studenti
    ph.getPersistenceManager().Find(CorsoStudente.class.getName(), new Class[]{Object.class}, new Object[]{this},
        studenti, mem.getReferenceForeignKey("CS_CORSO"));

  }

  @Override
  public void Remove(PersistenceHandler ph) throws PersistenceException {
    mem.loadKeyFromBean(this);
    ph.Remove(mem);
  }

  @Override
  public long getPropertyInfo(String propertyName, int info) {

    // InteractiveInterface.INFO_STATUS : consente di intervenire su obbligatorieta e editabilita di un campo
    if (InteractiveInterface.INFO_STATUS == info) {
      if ("code".equals(propertyName)) {
        return InteractiveInterface.VALUE_REQUIRED;
      }

      if ("description".equals(propertyName)) {
        return InteractiveInterface.VALUE_REQUIRED;
      }

      if ("startDate".equals(propertyName) || "endDate".equals(propertyName)) {
        return "S".equals(getRemote()) ? InteractiveInterface.VALUE_REQUIRED : 0;
      }
    }

    //return 0;

    // fixing per far funzionare il controllo di maxlength sui campi in base alle specifiche dell'XML
    // descriptor del bean : è necessario nella getPropertyInfo ri-demandare anche alla getObjectInfo
    Object result = getObjectInfo(DynamicInfoInterface.PROPERTY_INTERACTIVE + "." + info, propertyName);

    if (result instanceof Number) {
      return ((Number) result).longValue();
    } else {
      return 0;
    }

  }

  @Override
  public String validate() throws ValidateException {

    if (description.length() < 10) {
      //se sollevo ValidateException in pagina da un errore bloccante e impedisce il salvataggio
      throw new ValidateException("La stringa non è abbastanza lunga");
    }

    if (startDate == null || endDate == null) {
      // se torno una stringa salva comunque ma mi mostra un messsaggio di warning giallo.
      return "Sarebbe meglio valorizzare entrambe le date";
    }

    return null;
  }

}
