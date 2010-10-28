/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.editor.controller.curate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.editor.controller.BaseController;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.model.IntactObject;

import java.util.*;

/**
 * Keeps the changes for each annotated object by AC.
 * 
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("session")
public class CuratorContextController extends BaseController {

    @Autowired
    private PersistenceController persistenceController;

    public CuratorContextController() {
    }

    public Map<Object,UnsavedChangeManager> getUnsavedMap() {
        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");
        GeneralChangesController generalChangesController = (GeneralChangesController) getSpringContext().getBean("generalChangesController");

        return generalChangesController.getUnsavedMapForUser(userSessionController.getCurrentUser().getLogin());
    }

    public UnsavedChangeManager getUnsavedChangeManager(Object key) {
        Map<Object,UnsavedChangeManager> unsavedMap = getUnsavedMap();

        if (unsavedMap.containsKey(key)) {
            return unsavedMap.get(key);
        }

        UnsavedChangeManager unsavedChangeManager = new UnsavedChangeManager();

        if (key != null) {
            unsavedMap.put(key, unsavedChangeManager);
        }

        return unsavedChangeManager; 
    }

    public List<UnsavedChange> getAllChanges() {
        Map<Object,UnsavedChangeManager> unsavedMap = getUnsavedMap();

        List<UnsavedChange> unsavedChanges = new ArrayList<UnsavedChange>(unsavedMap.size() * 2);

        for (UnsavedChangeManager ucm : unsavedMap.values()) {
            unsavedChanges.addAll(ucm.getChanges());
        }

        return unsavedChanges;
    }

    public IntactObject findByAc(String ac) {
        for (UnsavedChange change : getAllChanges()) {
            if (ac.equals(change.getUnsavedObject().getAc())) {
                return change.getUnsavedObject();
            }
        }

        return null;
    }

    public void removeFromUnsaved(IntactObject object) {
        if (object.getAc() != null) {
            removeFromUnsavedByAc(object.getAc());
        }

        for (UnsavedChangeManager ucm : getUnsavedChangeManagers()) {
            ucm.removeFromUnsaved(object);
        }
    }

    public void removeFromUnsavedByAc(String ac) {
        for (UnsavedChangeManager ucm : getUnsavedChangeManagers()) {
            final Iterator<UnsavedChange> changeIterator = ucm.getChanges().iterator();

            while (changeIterator.hasNext()) {
                UnsavedChange unsavedChange = changeIterator.next();
                if (ac.equals(unsavedChange.getUnsavedObject().getAc())) {
                    changeIterator.remove();
                }
            }
        }
    }

    public Collection<UnsavedChangeManager> getUnsavedChangeManagers() {
        Map<Object,UnsavedChangeManager> unsavedMap = getUnsavedMap();
        return unsavedMap.values();
    }

    public void clear() {
        Map<Object,UnsavedChangeManager> unsavedMap = getUnsavedMap();
        unsavedMap.clear();
    }

    public String intactObjectSimpleName(IntactObject io) {
        if (io == null) return null;
        return io.getClass().getSimpleName().replaceAll("Impl", "");
    }
}
