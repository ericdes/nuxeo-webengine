package org.nuxeo.webengine.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;


public class WebCommentUtils {

    /**
     * Get all the users with a given permission for the corresponding workspace
     * */
    public static ArrayList<String> getUsersWithPermission(CoreSession session,
            DocumentModel doc, String permission) throws Exception {
        List<DocumentModel> parents = session.getParentDocuments(doc.getRef());
        for (DocumentModel documentModel : parents) {
            if (documentModel.getType().equals("Workspace")) {
                // TO DO: test for groups eg. administrators
                String[] moderators = documentModel.getACP().listUsernamesForPermission(
                        permission);
                return new ArrayList<String>(Arrays.asList(moderators));
            }
        }
        return new ArrayList<String>();
    }

    /**
     * @return true if the corresponding workspace is moderated
     * @throws Exception
     */
    public static boolean isCurrentModerated(CoreSession session,
            DocumentModel doc) throws Exception {
        return getUsersWithPermission(session, doc, WebCommentConstants.PERMISSION_MODERATE).size() >= 1 ? true : false;
    }

    /**
     * @return true if the current user is between moderators
     * @throws Exception
     */
    public static boolean isModeratedByCurrentUser(CoreSession session,
            DocumentModel doc) throws Exception {
        ArrayList<String> moderators = getUsersWithPermission(session, doc, WebCommentConstants.PERMISSION_MODERATE);
        if (moderators.contains(session.getPrincipal().getName())) {
            return true;
        }

        return false;
    }
    
    /**
     * @return true if the current user has comment permission on this document
     * @throws Exception
     */
    public static boolean currentUserHasCommentPermision(CoreSession session,
            DocumentModel doc) throws Exception {
        ArrayList<String> moderators = getUsersWithPermission(session, doc, WebCommentConstants.PERMISSION_COMMENT);
        if (moderators.contains(session.getPrincipal().getName())) {
            return true;
        }

        return false;

    }

}
