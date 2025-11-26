package sianc.sisar.wizard;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import PSGExt.profile.ProfileException;
import PSGExt.profile.ProfileManager;
import PSGExt.services.BinderInterface;
import PSGExt.services.ServiceContext;
import PSGExt.services.ServiceManager;
import PSGLibrary.JBFException;
import PSGLibrary.DB.ConnectionManager;
import PSGLibrary.DB.ResultSetHelper;
import PSGLibrary.DB.SequenceManager;
import PSGLibrary.util.ClassMapper;
import PSGLibrary.util.DateUtils;
import PSGLibrary.w3.controller.W3ControllerPersistent;
import sianc.sisar.corso.Corso;
import sianc.sisar.corso.Esame;


public class WizardW3 extends W3ControllerPersistent {

  private String description; //campo descrizione della pagina wizard
  private String teacher;     //campo insegnante del wizard form
  private String frequenza;   //campo frequenza del wizard form
  private Date dataEsameAnnuale; //campo data esame annuale del wizard form
  private Long dipartimento; //le possibili opzioni sono tabella generica (EG_ID di SI_ETABGEN è un NUMBER)

  private String frequenzaDefault;
  private Boolean isPrimoCaricamento=true;

  //proprietà per logging
  private static final Logger logger = Logger.getLogger(WizardW3.class);

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



  public String getFrequenza() {
    return frequenza;
  }

  public void setFrequenza(String frequenza) {
    this.frequenza = frequenza;
  }


  public Date getDataEsameAnnuale() {
    return dataEsameAnnuale;
  }

  public void setDataEsameAnnuale(Date dataEsameAnnuale) {
    this.dataEsameAnnuale = dataEsameAnnuale;
  }

  public String getFrequenzaDefault() {
    return frequenzaDefault;
  }

  public void setFrequenzaDefault(String frequenzaDefault) {
    this.frequenzaDefault = frequenzaDefault;
  }

  public Boolean getIsPrimoCaricamento() {
    return isPrimoCaricamento;
  }

  public void setIsPrimoCaricamento(Boolean isPrimoCaricamento) {
    this.isPrimoCaricamento = isPrimoCaricamento;
  }

  public Long getDipartimento() {
    return dipartimento;
  }

  public void setDipartimento(Long dipartimento) {
    this.dipartimento = dipartimento;
    //devo castare il dipartimento da int a stringa
    String dimensione = ClassMapper.classToClass(dipartimento, String.class);

    try {
      String rettore = ProfileManager.getProfileManager().getString(ProfileManager.PARAMETER_TYPE, "sianc.sisar.wizard.rettoreDip", user, dimensione);
      //prevalorizzo il docente col rettore letto da chiave di profilatura per dimensione, con dimensione = dipartimento appena selezionato
      this.teacher = rettore;

    } catch (ProfileException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  //esercizio chiavi profilatura con dimensione:
  // metodo per popolare le opzioni della combo dipartimento in pagina wizard.jsp
  public List getDipartimentoList() {
    List options = new ArrayList<>();
    //devo prendere le opzioni a partire da tabella generica!!!
    //ho due metodi possibili per leggere una tabella generica

    //METODO 1 : QUERY SU TABELLA SI_ETABGEN
    String sql = "SELECT EG_ID, EG_DESC1 FROM SI_ETABGEN WHERE EG_TCOD='SA_SARD_DIPARTIMENTI'";
    //classe utilita per eseguire select
    try {
      ResultSetHelper.fillListList(sql, options);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
    System.out.println("recuperati " + options.size() + " items dalla SI_ETABGEN per SA_SARD_DIPARTIMENTI");
    return options;
  }

  @Override
  public void init(HttpServletRequest req) throws Exception {
    super.init(req);

    setLastPageForward("/sianc/sisar/wizard/wizard.jsp");
  }


  @Override
  protected Object doRefresh(HttpServletRequest req) throws Exception {

    Object ret = super.doRefresh(req);

    if (getIsPrimoCaricamento()) {
      setIsPrimoCaricamento(false);
    }

    return ret;
  }


  @Override
  protected Object doAction(String action, HttpServletRequest req) throws Exception {
    setIsPrimoCaricamento(true);
    // GESTIONE LETTURA CHIAVE DI PROFILATURA : stringa per impostazione valore di default combo frequenza
    try {
      String freqDef = ProfileManager.getProfileManager().getString(ProfileManager.USER_TYPE,
          "sianc.sisar.wizard.defaultFrequenzaComboMC", user, null);
      if (freqDef != null && !freqDef.isEmpty()) {
        setFrequenzaDefault(freqDef);
      }
    } catch (ProfileException e1) {
      // throw new RuntimeException(e1);
      logger.error(e1, e1);
    }

     if("CREA_CORSO".equals(action)) {
        doCreaCorso(req);
        return getLastPageForward();
     }

     return super.doAction(action, req);
  }


  protected void doCreaCorso(HttpServletRequest req) {
    Connection connection = null;
    // prende i campi dalla HttpServletRequest come parametri
    // e li ribalta nelle properties del controller (description e teacher)
    setDataProperties(req,null);


    //GESTIONE LETTURA CHIAVE DI PROFILATURA
    try {
      Boolean valoreChiaveAbilitazioneWizardCorso = ProfileManager.getProfileManager().getBoolean(ProfileManager.USER_TYPE, "sianc.sisar.wizard.createCorsoButtonMC", user, null);
      if (!valoreChiaveAbilitazioneWizardCorso) {
        //messaggio di errore
        addError("GLOBAL", "Non sei abilitato all'utilizzo del Wizard di Creazione Corso");
      }
    } catch (ProfileException e1) {
      throw new RuntimeException(e1);
    }



    try {
        connection = ConnectionManager.getConnection(user, 999);

        ServiceContext context = new ServiceContext(user);
        context.setConnection(connection);

        //come creare istanza di un bean : la pk è opzionale perché viene usato questo metodo sia se voglio creare un nuovo oggetto (lo passo null la pk in questo caso) sia per recuperare una entità esistente in DB : es se ci passo "C1" mi ritorna l’entità corso con ID = C1
        BinderInterface binder = ServiceManager.getObject(Corso.class.getName(), null, context);

       //BinderInterface è un wrapper dell’oggetto reale che racchiude il vero oggetto
       // noi ci interfacciamo sempre ocn il binderInterface, non si accede mai direttamente all’oggetto
       binder.getBean(); // mi ritorna l’oggetto vero e proprio.

       // devo impostare le poprietà che mi ha passato l’utente con la pgina del wizard
       // primo param: nome della proprietà da settare nel bean
       // secondo param: valore da metterci: in questo caso la proprietà del controller in binding con il campo in pagina
       // terzo parametro : contesto
       binder.setPropertyValue("description", description, context);
       binder.setPropertyValue("teacher", teacher, context);
       binder.setPropertyValue("startDate", new Date(), context);

       //costruisco il codice del corso C_<sequenza>
       // creare sequienza in DB: CREATE SEQUENCE SEQ_SARD_CORSO_WIZARD;
       String codiceCorso = "C_" + SequenceManager.getNewValue(connection, "SEQ_SARD_CORSO_WIZARD"); //passo connessione e nome della sequence da leggere


       binder.setPropertyValue("code", codiceCorso, context);

       // salvo l’oggetto (TESTATA CORSO): uso il ServiceManager!!!
       ServiceManager.save(binder, context); //salvo oggetto wrappato da binder con connessione del context

       // **********************************
       // **** GESTIONE DETTAGLI ESAMI ****
       // **********************************
       // istanzio esame: c’è un solo costreuttore che prende in input un object (PARENT)
       // glielo passo tramite il contesto: setto nel contesto il padre prima di
       // chiamare la getObject del dettaglio : cosi si setta pure la proprietà corso (FK)
       context.setParameterValue(ServiceContext.PARENT_OBJECT, binder.getBean());

       if ("A".equals(frequenza)) {
          creaEsameCorso(context, dataEsameAnnuale, "Esame Annuale del corso " + codiceCorso );
       } else {

         int monthIncrement = 1;
         if ("T".equals(frequenza)) {
           monthIncrement = 3;
         } else if ("S".equals(frequenza)) {
           monthIncrement = 6;
         }

         for (int i = monthIncrement, j = 1; i <= 12; i = i + monthIncrement, j++) {
           String dateStr=  "01/"+i+"/2025";
           //DateUtils in PSGLibrary.util
           Date dataEsame = DateUtils.StrToDate(dateStr, "dd/MM/yyyy");
           String nomeEsame = "Esame " +j;
           creaEsameCorso(context, dataEsame, nomeEsame );
         }

       }

       connection.commit();

       // ESERCIZIO : mi rileggo l’oggetto appena creato e poi lo cancello per esercizio
       //BinderInterface retrievedObj = ServiceManager.getObject(Corso.class.getName(), codiceCorso, context);
       //ServiceManager.delete(retrievedObj, context);

        //connection.commit();

    }  catch (Exception e) {
        // throw new RuntimeException(e);
        // come mostrare errore gestito "graceful" all’utente :
        addError("GLOBAL" , "Errore durante la creazione del corso. " + e.getMessage());
        // mettere l’errore anche nei log applicativi : vedi sotto per istanziare il logger
        logger.error(e, e);
    } finally {
        ConnectionManager.releaseConnection(connection);
    }
 }


 // metodo di appoggio per creare un singolo esame
 protected void creaEsameCorso(ServiceContext context, Date dataEsame, String nomeEsame) throws JBFException {
   BinderInterface esameBinder = ServiceManager.getObject(Esame.class.getName(), null, context);
   esameBinder.setPropertyValue("description", nomeEsame, context);
   esameBinder.setPropertyValue("date", dataEsame, context);

   ServiceManager.save(esameBinder, context); // salvo dettaglio ESAME
 }

 // metodo che popola le opzioni nella nuova combo frequenza nella pagina wizard.jsp
 public List getFrequenzaList() {
   // key/description comma separeted
   String options = "M,Mensile,T,Trimestrale,S,Semestrale,A,Annuale";
   return PSGLibrary.util.ListUtils.createCodeValueList(options);
 }

}