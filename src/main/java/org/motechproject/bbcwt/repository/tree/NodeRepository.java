package org.motechproject.bbcwt.repository.tree;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.GenerateView;
import org.motechproject.bbcwt.domain.tree.Node;
import org.motechproject.bbcwt.repository.AbstractCouchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NodeRepository extends AbstractCouchRepository<Node> {

    @Autowired
    public NodeRepository(@Qualifier("bbcwtDbConnector") CouchDbConnector dbCouchDbConnector) {
        super(Node.class, dbCouchDbConnector);
    }

    @GenerateView
    public Node findByName(String treeName) {
        List<Node> trees = queryView("by_name", treeName);
        if(trees!=null && !trees.isEmpty()) {
            return trees.get(0);
        }
        return null;
    }
}