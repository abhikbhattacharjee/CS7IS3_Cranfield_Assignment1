package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Query {
    private static String INDEX_PATH = "index/";
    private static String OUTPUT_FILE = "output/results.txt";
    private static int NUM_RESULTS = 10;

    public static void searcher(String[] args) throws Exception {
        //System.out.println("Hello world!");
        String filePath = "../cran.qry";
//        String text = Files.readString(Paths.get(filePath));
        String text = Utils.readFullFile(filePath);
        int index = -1;
        int count = 0;

        Directory directory = FSDirectory.open(Paths.get(INDEX_PATH));
        DirectoryReader indexReader = DirectoryReader.open(directory);

        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        System.out.println("Please select the type of Similarity:\n 1. BM25 for BM25Similarity()\n 2. Boolean for BooleanSimilarity()\n"
        );
        Scanner myObj1 = new Scanner(System.in);
        String similarityChoice = myObj1.nextLine();

        switch (similarityChoice) {
            case "BM25":
                indexSearcher.setSimilarity(new BM25Similarity());
                break;
            case "Boolean":
                indexSearcher.setSimilarity(new BooleanSimilarity());
                break;
            default:
                indexSearcher.setSimilarity(new BM25Similarity());
        }

        System.out.println("Selected Similarity is: " + similarityChoice +"Similarity()");

        System.out.println("Please select the type of Analyzer for Query Parser:\n 1. Standard for StandardAnalyzer()\n 2. Simple for SimpleAnalyzer()\n 3. Whitespace for WhitespaceAnalyzer()\n"
        );
        Scanner myObj = new Scanner(System.in);
        String analyzerChoice = myObj.nextLine();
        Analyzer analyzer = null;

        switch (analyzerChoice) {
            case "Standard":
                analyzer = new StandardAnalyzer();
                break;
            case "Simple":
                analyzer = new SimpleAnalyzer();
                break;
            case "Whitespace":
                analyzer = new WhitespaceAnalyzer();
                break;
            default:
                analyzer = new StandardAnalyzer();
        }

        System.out.println("Selected Analyzer for Query Parser is: " + analyzerChoice +"Analyzer()");

        File resultFile = new File(OUTPUT_FILE);
        resultFile.getParentFile().mkdirs();
        PrintWriter writer = new PrintWriter(resultFile, StandardCharsets.UTF_8.name());

        HashMap<String, Float> fieldMap = new HashMap<>();
        fieldMap.put("title", 10f);
        fieldMap.put("author", 2f);
        fieldMap.put("words", 10f);

        ArrayList<Integer> indexData = new ArrayList<Integer>();

        do {
            if (index == -1)
                index = text.indexOf(".I");
            else
                index = text.indexOf(".I", (index + ".I".length()));
            if (index != -1) {
                indexData.add(index);
            }
            //System.out.println("Index: " + index);
            count++;
        }
        while (index != -1);
        if (index == -1) {
            indexData.add(text.length());
        }

        //System.out.println("Count: " + indexData.size());

        String word_doc = null;
        for (int i = 1; i < indexData.size(); i++) {
            String midText = text.substring(indexData.get(i - 1), indexData.get(i));
            //System.out.println(midText.substring(midText.indexOf(".A") + ".T\n".length(), midText.indexOf(".B")));
            String index_doc = midText.substring(midText.indexOf(".I") + ".I\n".length(), midText.indexOf(".W"));
            word_doc = midText.substring(midText.indexOf(".W") + ".W\n".length(), midText.length());
            //System.out.println(word_doc);
            //System.out.println(i);

            MultiFieldQueryParser MFQqueryParser = new MultiFieldQueryParser(
                    new String[]{"title", "author", "bibliograph", "text"}, analyzer, fieldMap);

            String queryString = QueryParser.escape(word_doc.trim());
            org.apache.lucene.search.Query query = MFQqueryParser.parse(queryString);
            //System.out.println(" >> " + index_doc);
            ScoreDoc[] sd = indexSearcher.search(query, NUM_RESULTS).scoreDocs;
            for (int j = 0; j < sd.length; j++) {
                Document hd = indexSearcher.doc(sd[j].doc);
                String line = (index_doc.trim().replaceAll("\n","")) + " 0 " +
                        (hd.get("docid").replaceAll("\n","")) + " 0 " + sd[j].score + " STANDARD";
                writer.println(line);
                /*
                 * index_doc (it has a return carriage and new line feed character)
                 *  0 hd.get("docid") (which also has a new line feed character)
                 */
            }

        }

        indexReader.close();
        writer.close();
        directory.close();
        System.out.println("Output: " + OUTPUT_FILE);


    }
}
