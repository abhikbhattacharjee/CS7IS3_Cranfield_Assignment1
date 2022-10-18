package org.example;

import ch.qos.logback.core.net.SyslogOutputStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
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
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import sun.print.PrinterJobWrapper;

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
    private static int NUM_RESULTS = 200;

    public static void searcher(String[] args) throws Exception {
        String filePath = "../cran.qry";
        String text = Utils.readFullFile(filePath);
        int index = -1;
        int count = 0;

        Directory directory = FSDirectory.open(Paths.get(INDEX_PATH));
        DirectoryReader indexReader = DirectoryReader.open(directory);

        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        System.out.println("Please select the type of Similarity:\n 1. BM25 for BM25Similarity()\n " +
                "2. Boolean for BooleanSimilarity()\n"
        );
        Scanner myObj1 = new Scanner(System.in);
        String similarityChoice = myObj1.nextLine();

        switch (similarityChoice) {
            case "BM25":
                indexSearcher.setSimilarity(new BM25Similarity());
                System.out.println("Selected Similarity is: BM25Similarity()");
                break;
            case "Boolean":
                indexSearcher.setSimilarity(new BooleanSimilarity());
                System.out.println("Selected Similarity is: BooleanSimilarity()");
                break;
            default:
                indexSearcher.setSimilarity(new BM25Similarity());
                System.out.println("Selected Default Similarity : BM25Similarity()");
        }


        System.out.println("Please select the type of Analyzer for Query Parser:\n 1. Standard for StandardAnalyzer()\n" +
                " 2. Simple for SimpleAnalyzer()\n 3. English for EnglishAnalyzer()\n 4. Whitespace for WhitespaceAnalyzer()\n"
        );
        Scanner myObj = new Scanner(System.in);
        String analyzerChoice = myObj.nextLine();
        Analyzer analyzer = null;

        switch (analyzerChoice) {
            case "Standard": {
                analyzer = new StandardAnalyzer();
                System.out.println("Selected Analyzer for Query Parser is: StandardAnalyzer()");
                break;
            }
            case "English": {
                analyzer = new EnglishAnalyzer(EnglishAnalyzer.getDefaultStopSet());
                System.out.println("Selected Analyzer for Query Parser is: EnglishAnalyzer()");
                break;
            }
            case "Simple":
                analyzer = new SimpleAnalyzer();
                System.out.println("Selected Analyzer for Query Parser is: SimpleAnalyzer()");
                break;
            case "Whitespace":
                analyzer = new WhitespaceAnalyzer();
                System.out.println("Selected Analyzer for Query Parser is: WhitespaceAnalyzer()");
                break;
            default: {
                System.out.println("Selected Default Analyzer for Query Parser : EnglishAnalyzer()");
                analyzer = new EnglishAnalyzer(EnglishAnalyzer.getDefaultStopSet());
            }
        }

        File resultFile = new File(OUTPUT_FILE);
        resultFile.getParentFile().mkdirs();
        PrintWriter writer = new PrintWriter(resultFile, StandardCharsets.UTF_8.name());

        ArrayList<Integer> indexData = new ArrayList<Integer>();

        do {
            if (index == -1)
                index = text.indexOf(".I");
            else
                index = text.indexOf(".I", (index + ".I".length()));
            if (index != -1) {
                indexData.add(index);
            }
            count++;
        }
        while (index != -1);
        if (index == -1) {
            indexData.add(text.length());
        }

        String word_doc = null;
        HashMap<String, Float> boostMap = new HashMap<>();
        boostMap.put("title", 100f);
        boostMap.put("author", 0.1f);
        boostMap.put("biblio", 0.1f);
        boostMap.put("words", 10000f);

        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(
                new String[]{"title", "author", "biblio", "words"}, analyzer, boostMap);
        int i;
        int j;
        for (i = 1; i < indexData.size(); i++) {
            String midText = text.substring(indexData.get(i - 1), indexData.get(i));
            String index_doc = midText.substring(midText.indexOf(".I") + ".I\n".length(), midText.indexOf(".W"));
            index_doc = index_doc.trim();
            word_doc = midText.substring(midText.indexOf(".W") + ".W\n".length(), midText.length());
            word_doc = word_doc.trim();

            String queryString = queryParser.escape(word_doc);
            org.apache.lucene.search.Query query = queryParser.parse(queryString);

            TopDocs topDocs = indexSearcher.search(query, NUM_RESULTS);
            int numTotalHits = Math.toIntExact(topDocs.totalHits.value);
            topDocs = indexSearcher.search(query, numTotalHits);
            ScoreDoc [] hits = topDocs.scoreDocs;

            for (j = 0; j < hits.length; j++) {
                Document hitDocument = indexSearcher.doc(hits[j].doc);
                writer.println(i + " 0 " + hitDocument.get("docid")
                         + " " + (j+1) + " " + hits[j].score + " STANDARD");
            }

        }
        indexReader.close();
        writer.close();
        directory.close();
        System.out.println("Output: " + OUTPUT_FILE);
    }
}
