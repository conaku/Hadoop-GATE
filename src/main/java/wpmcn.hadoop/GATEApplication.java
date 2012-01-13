package wpmcn.hadoop;

import gate.*;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class GATEApplication {
   private CorpusController application;
   private Corpus corpus;

   public GATEApplication(String gateHome) throws GateException, IOException {
      Gate.runInSandbox(true);
      Gate.setGateHome(new File(gateHome));
      Gate.setPluginsHome(new File(gateHome, "plugins"));
      Gate.init();
      URL applicationURL = new URL("file:" + new Path(gateHome, "application.xgapp").toString());
      application = (CorpusController) PersistenceManager.loadObjectFromUrl(applicationURL);
      corpus = Factory.newCorpus("Hadoop Corpus");
      application.setCorpus(corpus);
   }

   @SuppressWarnings({"unchecked"}) // Needed for corpus.add(document)
   public Document annotateDocument(String content) throws ResourceInstantiationException, ExecutionException {
      Document document = Factory.newDocument(content);
      corpus.add(document);
      application.execute();
      Factory.deleteResource(document);
      corpus.clear();
      return document;
   }

   public void close() {
      Factory.deleteResource(corpus);
      Factory.deleteResource(application);
   }

   static public void main(String[] args) throws Exception {
      String gateHome = args[0];
      String content = FileUtils.readFileToString(new File(args[1]));

      GATEApplication gate = new GATEApplication(gateHome);
      Document document = gate.annotateDocument(content);
      System.out.println(document);
      gate.close();
   }
}