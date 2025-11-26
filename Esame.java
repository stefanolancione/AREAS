package sianc.sisar.corso;

import java.util.Date;

import PSGLibrary.DB.OracleSequence;
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

public class Esame implements Persistent, ValidableInterface, DynamicInfoInterface, InteractiveInterface {

  private Long id; //PK (sequence SEQ_SA_SARD_ESAME)
  private String corso;             // proprietà di referenza verso il padre
  //proprieta da aggiungere per i bean di dettaglio (nel caso di master-detail)
  protected transient Corso parent; // proprietà transient che è riferimento al padre

  private String description;
  private Date date;
  private Integer maxStudent;

  protected transient static JDBCMementoDefs memDefs = new JDBCMementoDefs("SA_SARD_ESAME", new String[]{"ES_ID"}, "ES_UT_INS", "ES_DT_INS", "ES_UT_VAR", "ES_DT_VAR");
  protected transient JDBCMemento mem = new JDBCMemento(memDefs);

  // nuovo costruttore che riceve in input come parametro un Object che sarà il mio parent
  // costruttore da aggiungere per i bean di dettaglio (nel caso di master-detail)
  public Esame(Object parent) {
    this.parent = (Corso) parent;
    this.corso = this.parent.getCode(); //valorizzo la property di FK verso il padre
  }

  // getters e setters


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


  public String getDescription() {
    return description;
  }


  public void setDescription(String description) {
    this.description = description;
  }


  public Date getDate() {
    return date;
  }


  public void setDate(Date date) {
    this.date = date;
  }


  public Integer getMaxStudent() {
    return maxStudent;
  }


  public void setMaxStudent(Integer maxStudent) {
    this.maxStudent = maxStudent;
  }


  protected static DynamicInfoInterface infoDelegate;

  @Override
  //DynamicInfoUtils è quella di PSGLibrary.util
  public Object getObjectInfo(String info, String propertyName) {
    if (infoDelegate == null) {
      infoDelegate = DynamicInfoUtils.getBeanDynamicInfo(Esame.class);
    }
    return infoDelegate.getObjectInfo(info, propertyName);
  }

  @Override
  public String validate() throws ValidateException {
    return null;
  }

  @Override
  public void Store(PersistenceHandler ph) throws PersistenceException {
    mem.clear();
    mem.loadFromBean(this);

    // aggiunto per gestire la sequenza in DB per autogenerazione ID Esame in fase di salvataggio
    if (id == null) {
      mem.setProperty("ES_ID", new OracleSequence("SEQ_SA_SARD_ESAME"));
    }

    ph.Store(mem);

    // aggiunto per gestire la sequenza in DB: dopo il salvataggio mi riallineo il bean con il valore generato
    // dalla sequenza.
    id = mem.getPropertyAsLong("ES_ID");
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


  @Override
  public long getPropertyInfo(String propertyName, int info) {

    // InteractiveInterface.INFO_STATUS : consente di intervenire su obbligatorieta e editabilita di un campo
    if (InteractiveInterface.INFO_STATUS == info) {
      if ("description".equals(propertyName)) {
        return InteractiveInterface.VALUE_REQUIRED;
      }

      if ("date".equals(propertyName)) {
        return InteractiveInterface.VALUE_REQUIRED;
      }

    }

    // return 0;

    // fixing per far funzionare il controllo di maxlength sui campi in base alle specifiche dell'XML
    // descriptor del bean : è necessario nella getPropertyInfo ri-demandare anche alla getObjectInfo
    Object result = getObjectInfo(DynamicInfoInterface.PROPERTY_INTERACTIVE + "." + info, propertyName);

    if (result instanceof Number) {
      return ((Number) result).longValue();
    } else {
      return 0;
    }

  }

}