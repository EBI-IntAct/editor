package uk.ac.ebi.intact.editor.controller.curate.publication;

import edu.ucla.mbi.imex.central.ws.v20.IcentralFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.imex.DefaultImexCentralClient;
import psidev.psi.mi.jami.bridges.imex.ImexCentralClient;
import psidev.psi.mi.jami.bridges.imex.Operation;
import psidev.psi.mi.jami.bridges.imex.PublicationStatus;
import psidev.psi.mi.jami.bridges.imex.mock.MockImexCentralClient;

import java.util.Collection;
import java.util.Map;

/**
 * Wrapper of the Imex central client
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16/04/12</pre>
 */

public class ImexCentralClientWrapper implements ImexCentralClient{

    private static final Log log = LogFactory.getLog(ImexCentralClientWrapper.class);

    private final String username;
    private final String password;
    private final String endpoint;

    private ImexCentralClient imexCentralClient;
    
    public ImexCentralClientWrapper(String username, String password, String endpoint) {
        String localTrustStore = System.getProperty( "javax.net.ssl.trustStore" );
        String localTrustStorePwd = System.getProperty( "javax.net.ssl.keyStorePassword" );
        if (localTrustStore == null) {
            log.error( "It appears you haven't setup a local trust store (other than the one embedded in the JDK)." +
                    "\nShould you want to specify one, use: -Djavax.net.ssl.trustStore=<path.to.keystore> " +
                    "\nAnd if it is password protected, use: -Djavax.net.ssl.keyStorePassword=<password>" );
        } else {
            log.info( "Using local trust store: " + localTrustStore + (localTrustStorePwd == null ? " (no password set)" : " (with password set)" ) );
        }

        this.username = username;
        this.password = password;
        this.endpoint = endpoint;

        try {
            imexCentralClient = initialiseImexCentralClient(username, password, endpoint);
        } catch (Exception e) {
            log.error(e);
        }
    }
    
    @Override
    public String getEndpoint() {
        try {
            return getImexCentralClient().getEndpoint();
        } catch (BridgeFailedException e) {
            logImexError(e);
            return endpoint;
        }
    }

    @Override
    public Collection<psidev.psi.mi.jami.model.Publication> fetchPublicationsByOwner(String owner, int first, int max) throws BridgeFailedException {
        try {
            return getImexCentralClient().fetchPublicationsByOwner(owner, first, max);
        } catch (BridgeFailedException e) {
            throw logImexError(e);
        }
    }

    @Override
    public Collection<psidev.psi.mi.jami.model.Publication> fetchPublicationsByStatus(String status, int first, int max) throws BridgeFailedException {
        try {
            return getImexCentralClient().fetchPublicationsByStatus(status, first, max);
        } catch (BridgeFailedException e) {
            throw logImexError(e);
        }
    }

    @Override
    public psidev.psi.mi.jami.model.Publication updatePublicationStatus(String identifier, String source, PublicationStatus status) throws BridgeFailedException {
        try {
            return getImexCentralClient().updatePublicationStatus(identifier, source, status);
        } catch (BridgeFailedException e) {
            throw logImexError(e);
        }
    }

    @Override
    public psidev.psi.mi.jami.model.Publication updatePublicationAdminGroup(String identifier, String source, Operation operation, String group) throws BridgeFailedException {
        try {
            return getImexCentralClient().updatePublicationAdminGroup(identifier, source, operation, group);
        } catch (BridgeFailedException e) {
            throw logImexError(e);
        }
    }

    @Override
    public psidev.psi.mi.jami.model.Publication updatePublicationAdminUser(String identifier, String source, Operation operation, String user) throws BridgeFailedException {
        try {
            return getImexCentralClient().updatePublicationAdminUser(identifier, source, operation, user);
        } catch (BridgeFailedException e) {
            throw logImexError(e);
        }
    }

    @Override
    public psidev.psi.mi.jami.model.Publication updatePublicationIdentifier(String oldIdentifier, String oldSource, String newIdentifier, String source) throws BridgeFailedException {
        try {
            return getImexCentralClient().updatePublicationIdentifier(oldIdentifier, oldSource, newIdentifier, source);
        } catch (BridgeFailedException e) {
            throw logImexError(e);
        }
    }

    @Override
    public void createPublication(psidev.psi.mi.jami.model.Publication publication) throws BridgeFailedException {
        try {
            getImexCentralClient().createPublication(publication);
        } catch (BridgeFailedException e) {
            throw logImexError(e);
        }
    }

    @Override
    public psidev.psi.mi.jami.model.Publication createPublicationById(String identifier, String source) throws BridgeFailedException {
        try {
            return imexCentralClient.createPublicationById(identifier, source);
        } catch (BridgeFailedException e) {
            throw logImexError(e);
        }
    }

    @Override
    public psidev.psi.mi.jami.model.Publication fetchPublicationImexAccession(String identifier, String source, boolean assign) throws BridgeFailedException {
        try {
            return getImexCentralClient().fetchPublicationImexAccession(identifier, source, assign);
        } catch (BridgeFailedException e) {
            throw logImexError(e);
        }
    }

    @Override
    public psidev.psi.mi.jami.model.Publication fetchByIdentifier(String identifier, String source) throws BridgeFailedException {
        try {
            return getImexCentralClient().fetchByIdentifier(identifier, source);
        } catch (BridgeFailedException e) {
            throw logImexError(e);
        }
    }

    @Override
    public Collection<psidev.psi.mi.jami.model.Publication> fetchByIdentifiers(Map<String, Collection<String>> identifiers) throws BridgeFailedException {
        return getImexCentralClient().fetchByIdentifiers(identifiers);
    }

    private ImexCentralClient getImexCentralClient() throws BridgeFailedException {
        if (this.imexCentralClient == null) {
            this.imexCentralClient = initialiseImexCentralClient(username, password, endpoint);
        }
        return this.imexCentralClient;
    }

    private static BridgeFailedException logImexError(BridgeFailedException e) {
        if (e.getCause() instanceof IcentralFault) {
            IcentralFault f = (IcentralFault) e.getCause();
            log.error("Fault Code: " + f.getFaultInfo().getFaultCode());
            log.error("Fault Message: " + f.getFaultInfo().getMessage());
        }
        return e;
    }

    private static ImexCentralClient initialiseImexCentralClient(String username, String password, String endpoint) throws BridgeFailedException {
        if (username != null && password != null && endpoint != null && !username.isEmpty() && !password.isEmpty() && !endpoint.isEmpty()) {
            return new DefaultImexCentralClient(username, password, endpoint);
        } else {
            return new MockImexCentralClient();
        }
    }
}
