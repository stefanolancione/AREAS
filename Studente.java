package sianc.sisar.studente;

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

public class Studente implements Persistent, ValidableInterface, DynamicInfoInterface, InteractiveInterface {

  private Long id;
  private String name;
  private String surname;
  private Date startDate;
  private Date endDate;
  //aggiunta lookup
  private String cittadinanza;


  protected transient static JDBCMementoDefs memDefs = new JDBCMementoDefs("SA_SARD_STUDENTE",
      new String[]{"ST_ID"}, "ST_UT_INS", "ST_DT_INS", "ST_UT_VAR", "ST_DT_VAR");
  protected transient JDBCMemento mem = new JDBCMemento(memDefs);

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
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




  public String getCittadinanza() {
    return cittadinanza;
  }


  public void setCittadinanza(String cittadinanza) {
    this.cittadinanza = cittadinanza;
  }



  protected static DynamicInfoInterface infoDelegate;

  @Override
  public Object getObjectInfo(String info, String propertyName) {

    //ES 5 : introduzione LOOKUP con FK verso SA_CITTADINANZA
    //       collego la proprieta cittadinanza alla lookup censita con codice SA_CITTADINANZA in SI_LOOKUP
    if (DynamicInfoInterface.PROPERTY_LOOKUP.equals(info)) {
      if ("cittadinanza".equals(propertyName)) {
        return "SA_CITTADINANZA";  //LK_CODICE di SI_LOOKUP
      }
    }

    if (infoDelegate == null) {
      infoDelegate = DynamicInfoUtils.getBeanDynamicInfo(Studente.class);
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
      mem.setProperty("ST_ID", new OracleSequence("SEQ_SA_SARD_STUDENTE"));
    }


    ph.Store(mem);
    // gestione id generato con sequenza in DB :
    id=mem.getPropertyAsLong("ST_ID");
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
      if ("id".equals(propertyName)) {
        return InteractiveInterface.VALUE_LOCKED;
      }
    }

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



