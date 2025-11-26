package sianc.sisar.corso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import PSGLibrary.DB.LookupManager;
import PSGLibrary.DB.OracleSequence;
import PSGLibrary.Persistence.JDBCMemento;
import PSGLibrary.Persistence.JDBCMementoDefs;
import PSGLibrary.Persistence.PersistenceException;
import PSGLibrary.Persistence.PersistenceHandler;
import PSGLibrary.Persistence.Persistent;
import PSGLibrary.util.DynamicInfoInterface;
import PSGLibrary.util.DynamicInfoUtils;
import PSGLibrary.util.ValidableInterface;
import PSGLibrary.util.ValidateException;

public class CorsoStudente implements Persistent, ValidableInterface, DynamicInfoInterface {

  private Long id;

  private String corso;
  private Long studente;
  private Integer voto;

  //riferimento al padre : TRANSIENT non va deserializzato
  private transient Corso parent;


  protected transient static JDBCMementoDefs memDefs = new JDBCMementoDefs("SA_SARD_CORSO_STUDENTE",
      new String[]{"CS_ID"}, "CS_UT_INS", "CS_DT_INS", "CS_UT_VAR", "CS_DT_VAR");
  protected transient JDBCMemento mem = new JDBCMemento(memDefs);

  //e' un bean di dettaglio : devo mettere il costruttore con il parametro Object che è il padre
  public CorsoStudente(Object parent) {
    this.parent = (Corso) parent;
    this.corso = this.parent.getCode();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCorso() {
    return corso;
  }

  public void setCorso(String corso) {
    this.corso = corso;
  }

  public Long getStudente() {
    return studente;
  }

  public void setStudente(Long studente) {
    this.studente = studente;
  }

  public Integer getVoto() {
    return voto;
  }

  public void setVoto(Integer voto) {
    this.voto = voto;
  }


  protected static DynamicInfoInterface infoDelegate;

  @Override
  public Object getObjectInfo(String info, String propertyName) {

    if (DynamicInfoInterface.PROPERTY_LOOKUP.equals(info)) {
      if ("studente".equals(propertyName)) {
        return "SA_SARD_STUDENTE_LK";
      }
    }

    // **** PASSAGGIO FILTRI A UNA LOOKUP *****
    if (DynamicInfoInterface.PROPERTY_LOOKUP_FILTERS.equals(info)) {
      if ("studente".equals(propertyName)) {
        Map<String, Object> filters = new HashMap<>();
        //filterName puo valere o una delle costati definite in LookupManager
        //oppure una stringa con il nome della colonna in DB nella tabella
        //puntata dalla lookup
        filters.put(LookupManager.DATE_ENABLED_FILTER, new Date());
        //altri esempi
        //filters.put("ST_NAME", "Giuseppe"); // fa una uguaglianza secca
        //filters.put("TO_CHAR(ST_START_DATE,’yyyy’)", "2022"); // fa una uguaglianza secca torna iscritti nel 2022

        return filters;
      }
    }


    if (infoDelegate == null) {
      infoDelegate = DynamicInfoUtils.getBeanDynamicInfo(CorsoStudente.class);
    }

    return infoDelegate.getObjectInfo(info, propertyName);
  }

  @Override
  public String validate() throws ValidateException {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public void Store(PersistenceHandler ph) throws PersistenceException {
    mem.clear();
    mem.loadFromBean(this);

    //gestione generazione id con sequenza in db :
    if (id==null) {
      mem.setProperty("CS_ID", new OracleSequence("SEQ_SA_SARD_CORSO_STUDENTE"));
    }

    ph.Store(mem);
    // gestione id generato con sequenza in DB :
    id=mem.getPropertyAsLong("CS_ID");
  }

  @Override
  public void Retrieve(PersistenceHandler ph) throws PersistenceException {
    mem.clear();

    mem.loadKeyFromBean(this);
    ph.Retrieve(mem);
    mem.saveToBean(this);

  }

  @Override
  public void Remove(PersistenceHandler ph) throws PersistenceException {
    mem.loadKeyFromBean(this);
    ph.Remove(mem);
  }



}
