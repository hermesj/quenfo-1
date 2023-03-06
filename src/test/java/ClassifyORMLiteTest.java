import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import quenfo.de.uni_koeln.spinfo.classification.core.classifier.AbstractClassifier;
import quenfo.de.uni_koeln.spinfo.classification.core.classifier.model.Model;
import quenfo.de.uni_koeln.spinfo.classification.core.data.ExperimentConfiguration;
import quenfo.de.uni_koeln.spinfo.classification.core.data.FeatureUnitConfiguration;
import quenfo.de.uni_koeln.spinfo.classification.core.distance.Distance;
import quenfo.de.uni_koeln.spinfo.classification.core.feature_engineering.feature_weighting.AbstractFeatureQuantifier;
import quenfo.de.uni_koeln.spinfo.classification.core.feature_engineering.feature_weighting.LogLikeliHoodFeatureQuantifier;
import quenfo.de.uni_koeln.spinfo.classification.jasc.data.JASCClassifyUnit;
import quenfo.de.uni_koeln.spinfo.classification.jasc.workflow.ORMDatabaseClassifier;
import quenfo.de.uni_koeln.spinfo.classification.zone_analysis.classifier.ZoneKNNClassifier;
import quenfo.de.uni_koeln.spinfo.core.helpers.PropertiesHandler;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

class ClassifyORMLiteTest {

    static String evaluationDBPath = "jdbc:sqlite:src/test/resources/sqlite/orm/quenfo_evaluation.db";
    static ConnectionSource evaluationDB;

    static String modelsDBPath = "jdbc:sqlite:src/test/resources/models.db";

    static String silverDBPath = "jdbc:sqlite:src/test/resources/quenfo_silverstandard.db";
    static ConnectionSource silverDB;

    // fixed parameters for creating the silverstandard
    static String trainingdataFile = "src/test/resources/resources/classification/trainingSets/trainingdata_anonymized.tsv";
    static int queryLimit = 800;
    static int startPos = 0;
    static int fetchSize = 200;

    static boolean normalize = true;
    static boolean stem = true;
    static boolean filterSW = true;
    static int[] nGrams = {3,4};
    static boolean continousNGrams = true;
    static int miScore = 0;
    static boolean suffixTree = false;

    static double evalTimeMillis;


    @BeforeAll
    static void main() throws SQLException, IOException /*, ClassNotFoundException*/ {

        File configFolder = new File("src/test/resources/config");

        PropertiesHandler.initializeForTests(configFolder);

        silverDB = new JdbcConnectionSource(silverDBPath);
        // TODO copy silverstandard to evaluation file
        // create connection to jobad sqlite database
        evaluationDB = new JdbcConnectionSource(evaluationDBPath);

        // delete previous test results
        try {
            TableUtils.clearTable(evaluationDB, JASCClassifyUnit.class);
        } catch (SQLException e) {
            System.err.println("Noch keine Daten zum Überschreiben vorhanden.");
        }

        long before = System.currentTimeMillis();

        // configurate classification
        FeatureUnitConfiguration fuc = new FeatureUnitConfiguration(normalize, stem, filterSW, nGrams, continousNGrams,
                miScore, suffixTree);
        AbstractFeatureQuantifier fq = new LogLikeliHoodFeatureQuantifier();
        AbstractClassifier classifier = new ZoneKNNClassifier(false, 5, Distance.COSINUS);
        ExperimentConfiguration config = new ExperimentConfiguration(fuc, fq, classifier, new File(trainingdataFile),
                null);

        ORMDatabaseClassifier ormClassifier = new ORMDatabaseClassifier(evaluationDB, queryLimit, fetchSize,
                startPos, trainingdataFile);

        // create connection to model database
        ConnectionSource modelsDB = new JdbcConnectionSource(modelsDBPath);
        Dao<? extends Model, ?> modelDao = classifier.getModelDao(modelsDB);
        if (!modelDao.isTableExists())
            TableUtils.createTable(modelDao);

        Model trainedModel = ormClassifier.train(config, modelDao);

        // classify orm-objects with model
        ormClassifier.classify(trainedModel, config);

        long after = System.currentTimeMillis();
        evalTimeMillis = (double) after - before;
        double time = (evalTimeMillis / 1000) / 60;
        if (time > 60) {
            System.out.println("\nfinished Classification in " + (time / 60) + "hours");
        } else {
            System.out.println("\nfinished Classification in " + time + " minutes");
        }
        evaluationDB.close();
        modelsDB.close();
    }

    @Test
    void compareTime() {
        double silverTimeMillis = 297907.0;
        System.out.println("Silvertime:\t" + silverTimeMillis + "\nNow:\t" + evalTimeMillis);

        // neue Zeit ist kleiner oder gleich Silver + 10 Sek.
        Assertions.assertTrue(evalTimeMillis <= (silverTimeMillis + 10000d),
                "Took longer than expected");

    }

    /**
     * compares the number of generated paragraphs with silverstandard
     */
    @Test
    void compareSize() throws SQLException {

        // create the DAOs
        Dao<JASCClassifyUnit, String> evalDao = DaoManager.createDao(evaluationDB, JASCClassifyUnit.class);
        Dao<JASCClassifyUnit, String> silverDao = DaoManager.createDao(silverDB, JASCClassifyUnit.class);

        long evalCount = evalDao.countOf();
        long silverCount = silverDao.countOf();

        Assertions.assertEquals(silverCount, evalCount,
                "Wrong number of classified units");
    }

    @Test
    void compareCatDistribution() throws SQLException{

        // create the DAOs
        Dao<JASCClassifyUnit, String> evalDao = DaoManager.createDao(evaluationDB, JASCClassifyUnit.class);
        Dao<JASCClassifyUnit, String> silverDao = DaoManager.createDao(silverDB, JASCClassifyUnit.class);

        String[] columnNames = {"ClassONE", "ClassTWO", "ClassTHREE", "ClassFOUR"};
        long currCountEval;
        long currCountSilver;
        for (String c: columnNames) {
            currCountEval = evalDao.queryBuilder()
                    .where().eq(c, true).countOf();

            currCountSilver = silverDao.queryBuilder()
                    .where().eq(c, true).countOf();
            Assertions.assertEquals(currCountSilver, currCountEval, "Category " + c + " is not identical");
        }






    }


}