package uk.ac.ebi.intact.service.complex.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persistence.dao.InteractionDao;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexFieldNames;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
public class SearchController {
    /********************************/
    /*      Private attributes      */
    /********************************/
    @Autowired
    private DataProvider dataProvider ;

    @PersistenceContext(unitName="intact-core-default")
    private EntityManager complexManager;

    @Autowired
    private DaoFactory daoFactory;

    private static final Log log = LogFactory.getLog(SearchController.class);

     /*
     -- BASIC KNOWLEDGE ABOUT SPRING MVC CONTROLLERS --
      * They look like the next one:
      @RequestMapping(value = "/<path to listen>/{<variable>}")
	  public <ResultType> search(@PathVariable String <variable>) {
          ...
	  }

	  * First of all, we have the @RequestMapping annotation where you can
	    use these options:
	     - headers: Same format for any environment: a sequence of
	                "My-Header=myValue" style expressions
	     - method: The HTTP request methods to map to, narrowing the primary
	               mapping: GET, POST, HEAD, OPTIONS, PUT, DELETE, TRACE.
	     - params: Same format for any environment: a sequence of
	               "myParam=myValue" style expressions
	     - value: Ant-style path patterns are also supported (e.g. "/myPath/*.do").

      * Next we have the function signature, with the result type to return,
        the name of the function and the parameters to it. We could see the
        @PathVariable in the parameters it is to say that the content between
        { and } is assigned to this variable. NOTE: They must have the same name

        Moreover, we can have @RequestedParam if we need to read or use a parameter
        provided using "?name=value" way. WE WANT TO DO THAT WITH THE FORMAT,
        BUT THIS PARAMETER IS CONTROLLED BY THE ContentNegotiatingViewResolver BEAN
        IN THE SPRING FILE.
     */

    /*******************************/
    /*      Protected methods      */
    /*******************************/
    // This method controls the first and number parameters and retrieve data
    protected ComplexRestResult query(String query, String first, String number) {
        // Get parameters (if we have them)
        int f, n;
        // If we have first parameter parse it to integer
        if ( first != null ) f = Integer.parseInt(first);
            // else set first parameter to 0
        else f = 0;
        // If we have number parameter parse it to integer
        if ( number != null ) n = Integer.parseInt(number);
            // else set number parameter to max integer - first (to avoid problems)
        else n = Integer.MAX_VALUE - f;
        try{
            // Retrieve data using that parameters and return it
            return this.dataProvider.getData( query, f, n );
        }
        catch (SolrServerException e){
            if ( log.isInfoEnabled() )
                log.info( "DataProvider error, it could not connect to Solr Server", e);
            return null;
        }
    }

    // This method is to force to query only for a list of fields
    protected String improveQuery(String query, List<String> fields) {
        StringBuilder improvedQuery = new StringBuilder();
        for ( String field : fields ) {
            improvedQuery.append(field)
                         .append(":(")
                         .append(query)
                         .append(")");
        }
        return improvedQuery.toString();
    }

    /****************************/
    /*      Public methods      */
    /****************************/
    /*
     - We can access to that method using:
         http://<servername>:<port>/search/<something to query>
       and
         http://<servername>:<port>/search/<something to query>?format=<type>
     - If we do not use the format parameter we take the values in the headers
     - If we use the format parameter we do not mind in the headers
     - Only listen request via GET never via POST.
     - Does not change the query.
     */
    @RequestMapping(value = "/search/{query}", method = RequestMethod.GET)
	public ComplexRestResult search(@PathVariable String query,
                                    @RequestParam (required = false) String first,
                                    @RequestParam (required = false) String number) {
        return query(query, first, number);
	}

    /*
     - We can access to that method using:
         http://<servername>:<port>/interactor/<something to query>
       and
         http://<servername>:<port>/interactor/<something to query>?format=<type>
     - If we do not use the format parameter we take the values in the headers
     - If we use the format parameter we do not mind in the headers
     - Only listen request via GET never via POST.
     - Force to query only in the id, alias and pxref fields.
     */
    @RequestMapping(value = "/interactor/{query}", method = RequestMethod.GET)
    public ComplexRestResult searchInteractor(@PathVariable String query,
                                              @RequestParam (required = false) String first,
                                              @RequestParam (required = false) String number) {

        // Query improvement. Force to query only in the id, alias and pxref
        // fields.
        List<String> fields = new ArrayList<String>();
        fields.add(ComplexFieldNames.INTERACTOR_ID);
        fields.add(ComplexFieldNames.INTERACTOR_ALIAS);
        fields.add(ComplexFieldNames.INTERACTOR_XREF);
        // Retrieve data using that parameters and return it
        return query(improveQuery(query, fields), first, number);
    }

    /*
     - We can access to that method using:
         http://<servername>:<port>/complex/<something to query>
       and
         http://<servername>:<port>/complex/<something to query>?format=<type>
     - If we do not use the format parameter we take the values in the headers
     - If we use the format parameter we do not mind in the headers
     - Only listen request via GET never via POST.
     - Force to query only in the complex_id, complex_alias and complex_xref
       fields.
     */
    @RequestMapping(value = "/complex/{query}", method = RequestMethod.GET)
    public ComplexRestResult searchInteraction(@PathVariable String query,
                                               @RequestParam (required = false) String first,
                                               @RequestParam (required = false) String number) {

        // Query improvement. Force to query only in the complex_id,
        // complex_alias and complex_xref fields.
        List<String> fields = new ArrayList<String>();
        fields.add(ComplexFieldNames.COMPLEX_ID);
        fields.add(ComplexFieldNames.COMPLEX_ALIAS);
        fields.add(ComplexFieldNames.COMPLEX_XREF);
        // Retrieve data using that parameters and return it
        return query(improveQuery(query, fields), first, number);
    }

    /*
     - We can access to that method using:
         http://<servername>:<port>/organism/<something to query>
       and
         http://<servername>:<port>/organism/<something to query>?format=<type>
     - If we do not use the format parameter we take the values in the headers
     - If we use the format parameter we do not mind in the headers
     - Only listen request via GET never via POST.
     - Force to query only in the organism_name and species fields.
     */
    @RequestMapping(value = "/organism/{query}", method = RequestMethod.GET)
    public ComplexRestResult searchOrganism(@PathVariable String query,
                                            @RequestParam (required = false) String first,
                                            @RequestParam (required = false) String number) {

        // Query improvement. Force to query only in the organism_name and
        // species (complex_organism) fields.
        List<String> fields = new ArrayList<String>();
        fields.add(ComplexFieldNames.ORGANISM_NAME);
        fields.add(ComplexFieldNames.COMPLEX_ORGANISM);
        // Retrieve data using that parameters and return it
        return query(improveQuery(query, fields), first, number);
    }

    @RequestMapping(value = "/details/{ac}", method = RequestMethod.GET)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public ComplexDetails retrieveComplex(@PathVariable String ac) {
        InteractionDao interactionDao = daoFactory.getInteractionDao();
        InteractionImpl complex = interactionDao.getByAc(ac);
        ComplexDetails details = null;
        // Function
        if ( complex != null ) {
            details = new ComplexDetails();
            CvTopic cvTopic = null ;
            for ( Annotation annotation : complex.getAnnotations ( ) ) {
                cvTopic = annotation != null ? annotation.getCvTopic ( ) : null ;
                if ( cvTopic != null && cvTopic.getShortLabel ( ) .equalsIgnoreCase( "curated-complex" ) && annotation.getAnnotationText() != null) {
                    details.setFunction ( annotation.getAnnotationText ( ) ) ;
                }
                else if ( annotation.getCvTopic() != null && annotation.getCvTopic().getIdentifier() != null && annotation.getCvTopic().getIdentifier().equals("MI:0629") ) {
                        details.setProperties(annotation.getAnnotationText());
                }
            }
            // Names
            String firstRecommended=null;
            String firstSystematic=null;
            String firstComplexSynonym=null;
            String firstAlias=null;

            for ( Alias alias : complex.getAliases ( ) ) {
                if (alias.getName() != null){
                    if (alias.getCvAliasType() != null){
                        CvAliasType type = alias.getCvAliasType();
                        if ( firstRecommended == null && "MI:1315".equals(type.getIdentifier()) ){
                            firstRecommended = alias.getName();
                        }
                        else if ( firstSystematic == null && "MI:1316".equals(type.getIdentifier()) ){
                            firstSystematic = alias.getName();
                        }
                        else if ( "MI:0673".equals(type.getIdentifier()) ){
                            if ( firstComplexSynonym == null ) firstComplexSynonym = alias.getName();
                            details.addSynonym(alias.getName());
                        }
                    }
                    else if (firstAlias == null){
                        firstAlias = alias.getName();
                    }
                }
            }

            if ( firstRecommended != null ) {
                details.setName(firstRecommended);
            }
            else if ( firstSystematic != null ) {
                details.setName(firstSystematic);
            }
            else if ( firstComplexSynonym != null ) {
                details.setName(firstComplexSynonym);
            }
            else if ( firstAlias != null ) {
                details.setName(firstAlias);
            }
            else{
                details.setName(complex.getShortLabel());
            }

            details.setSystematicName(firstSystematic);
            if (! complex.getExperiments().isEmpty()){
                Experiment exp = complex.getExperiments().iterator().next();
                BioSource bioSource = exp.getBioSource();
                if ( bioSource != null ){
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(bioSource.getFullName() != null ? bioSource.getFullName() : bioSource.getShortLabel());
                    stringBuilder.append("(").append(bioSource.getTaxId()).append(")");
                    details.setSpecie(stringBuilder.toString());
                }
            }
            details.setAc(complex.getAc());


            Collection<ComplexDetailsParticipants> participants = details.getParticipants();
            ComplexDetailsParticipants part;
            for ( Component component : complex.getComponents() ) {
                part = new ComplexDetailsParticipants();
                Interactor interactor = component.getInteractor();
                if ( interactor != null ) {
                    part.setInteractorAC(interactor.getAc());
                    part.setDescription(interactor.getFullName());
                    Xref xref = null;
                    if (CvObjectUtils.isProteinType(interactor.getCvInteractorType())) {
                        xref = ProteinUtils.getUniprotXref(interactor);

                        String geneName = null;
                        for (Alias alias : interactor.getAliases()) {
                            if ( alias.getCvAliasType() != null && CvAliasType.GENE_NAME_MI_REF.equals(alias.getCvAliasType().getIdentifier())) {
                                geneName = alias.getName();
                                break;
                            }
                        }
                        part.setName(geneName !=null ? geneName : interactor.getShortLabel());

                    }
                    else if( CvObjectUtils.isSmallMoleculeType(interactor.getCvInteractorType()) || CvObjectUtils.isPolysaccharideType(interactor.getCvInteractorType()) ){
                        xref = SmallMoleculeUtils.getChebiXref(interactor);
                        part.setName(interactor.getShortLabel());
                    }
                    else {
                        part.setName(interactor.getShortLabel());
                        xref = XrefUtils.getIdentityXref(interactor, CvDatabase.ENSEMBL_MI_REF);
                        xref = xref != null ? xref : XrefUtils.getIdentityXref(interactor, "MI:1013");
                    }
                    if (xref != null) {
                        part.setIdentifier(xref.getPrimaryId());
                        for ( Annotation annotation : xref.getCvDatabase().getAnnotations() ) {
                            if ( annotation.getCvTopic() != null && CvTopic.SEARCH_URL_MI_REF.equals(annotation.getCvTopic().getIdentifier()) ) {
                                part.setIdentifierLink(annotation.getAnnotationText().replaceAll("\\$*\\{ac\\}", xref.getPrimaryId()));
                            }
                        }
                    }
                    part.setInteractorType(interactor.getCvInteractorType().getFullName() != null ? interactor.getCvInteractorType().getFullName() : interactor.getCvInteractorType().getShortLabel());
                    part.setInteractorTypeMI(interactor.getCvInteractorType().getIdentifier());
                    for ( Annotation annotation : interactor.getCvInteractorType().getAnnotations() ) {
                       if( annotation.getCvTopic().getShortLabel().equalsIgnoreCase(CvTopic.DEFINITION) ){
                            part.setInteractorTypeDefinition(annotation.getAnnotationText());
                        }
                    }
                }
                part.setStochiometry(component.getStoichiometry() == 0.0f ? null : Float.toString(component.getStoichiometry()));
                if (component.getCvBiologicalRole() != null) {
                    part.setBioRole(component.getCvBiologicalRole().getFullName() != null ? component.getCvBiologicalRole().getFullName() : component.getCvBiologicalRole().getShortLabel());
                    part.setBioRoleMI(component.getCvBiologicalRole().getIdentifier());
                    for ( Annotation annotation : component.getCvBiologicalRole().getAnnotations() ) {
                        if( annotation.getCvTopic().getShortLabel().equalsIgnoreCase(CvTopic.DEFINITION) ){
                            part.setBioRoleDefinition(annotation.getAnnotationText());
                        }
                    }
                }

                for( Feature feature : component.getFeatures() ) {
                    ComplexDetailsFeatures complexDetailsFeatures = new ComplexDetailsFeatures();
                    if ( feature.getBoundDomain() != null ) {
                        part.getLinkedFeatures().add(complexDetailsFeatures);
                        Component featureComponent = feature.getBoundDomain().getComponent();
                        if (featureComponent != null) {
                            Interactor linkedInteractor = featureComponent.getInteractor();
                            if ( linkedInteractor != null ) {
                                Xref xref = null;
                                if (CvObjectUtils.isProteinType(linkedInteractor.getCvInteractorType())) {
                                    xref = ProteinUtils.getUniprotXref(linkedInteractor);

                                }
                                else if( CvObjectUtils.isSmallMoleculeType(linkedInteractor.getCvInteractorType()) || CvObjectUtils.isPolysaccharideType(linkedInteractor.getCvInteractorType()) ){
                                    xref = SmallMoleculeUtils.getChebiXref(linkedInteractor);
                                }
                                else {
                                    xref = XrefUtils.getIdentityXref(linkedInteractor, CvDatabase.ENSEMBL_MI_REF);
                                    xref = xref != null ? xref : XrefUtils.getIdentityXref(linkedInteractor, "MI:1013");
                                }
                                if (xref != null) {
                                    complexDetailsFeatures.setParticipantId(xref.getPrimaryId());
                                }
                            }
                        }
                    }
                    else {
                        part.getOtherFeatures().add(complexDetailsFeatures);
                    }
                    if (feature.getCvFeatureType() != null) {
                        complexDetailsFeatures.setFeatureType(feature.getCvFeatureType().getFullName() != null ? feature.getCvFeatureType().getFullName() : feature.getCvFeatureType().getShortLabel());
                        complexDetailsFeatures.setFeatureTypeMI(feature.getCvFeatureType().getIdentifier());
                        for ( Annotation annotation : component.getCvBiologicalRole().getAnnotations() ) {
                            if( annotation.getCvTopic().getShortLabel().equalsIgnoreCase(CvTopic.DEFINITION) ){
                                complexDetailsFeatures.setFeatureTypeDefinition(annotation.getAnnotationText());
                            }
                        }
                    }
                    for ( Range range : feature.getRanges() ) {
                        complexDetailsFeatures.getRanges().add(FeatureUtils.convertRangeIntoString(range));
                    }
                }

                participants.add(part);
            }

            Collection<ComplexDetailsCrossReferences> crossReferences = details.getCrossReferences();
            ComplexDetailsCrossReferences cross;
            for ( Xref xref : complex.getXrefs()) {
                cross = new ComplexDetailsCrossReferences();
                CvDatabase cvDatabase = xref.getCvDatabase();
                CvXrefQualifier cvXrefQualifier = xref.getCvXrefQualifier();
                String primaryId = xref.getPrimaryId();
                String secondayId = xref.getSecondaryId();
                cross.setIdentifier(primaryId);
                cross.setDescription(secondayId);
                cross.setDatabase(cvDatabase.getFullName() != null ? cvDatabase.getFullName() : cvDatabase.getShortLabel());
                cross.setDbMI(cvDatabase.getIdentifier());
                for ( Annotation annotation : cvDatabase.getAnnotations() ) {
                    if ( annotation.getCvTopic() != null && CvTopic.SEARCH_URL_MI_REF.equals(annotation.getCvTopic().getIdentifier()) ) {
                        cross.setSearchURL(annotation.getAnnotationText().replaceAll("\\$*\\{ac\\}",primaryId));
                    }
                    else if( annotation.getCvTopic().getShortLabel().equalsIgnoreCase(CvTopic.DEFINITION) ){
                        cross.setDbdefinition(annotation.getAnnotationText());
                    }
                }
                if( cvXrefQualifier != null ) {
                    cross.setQualifier(cvXrefQualifier.getFullName() != null ? cvXrefQualifier.getFullName() : cvXrefQualifier.getShortLabel());
                    cross.setQualifierMI(cvXrefQualifier.getIdentifier());
                    for ( Annotation annotation : cvXrefQualifier.getAnnotations() ) {
                        if( annotation.getCvTopic().getShortLabel().equalsIgnoreCase(CvTopic.DEFINITION) ){
                            cross.setQualifierDefinition(annotation.getAnnotationText());
                        }
                    }
                }
                crossReferences.add(cross);
            }

        }
        return details;
    }
}
