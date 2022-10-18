package org.example;

import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;



public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        System.out.println("Please select the type of Analyzer for Index Writer:\n 1. Standard for StandardAnalyzer()\n " +
                "2. Simple for SimpleAnalyzer()\n 3. English for EnglishAnalyzer()\n 4. Whitespace for WhitespaceAnalyzer()\n"
        );
        Scanner myObj = new Scanner(System.in);
        String analyzerChoice = myObj.nextLine();

        String INDEX_DIRECTORY = "./index";

        Analyzer analyzer = null;

        switch (analyzerChoice) {
            case "Standard":
                analyzer = new StandardAnalyzer();
                System.out.println("Selected Analyzer for Index Writer is: StandardAnalyzer()");
                break;
            case "English": {
                analyzer = new EnglishAnalyzer(EnglishAnalyzer.getDefaultStopSet());
                System.out.println("Selected Analyzer for Index Writer is: EnglishAnalyzer()");
                break;
            }
            case "Simple":
                analyzer = new SimpleAnalyzer();
                System.out.println("Selected Analyzer for Index Writer is: SimpleAnalyzer()");
                break;
            case "Whitespace":
                analyzer = new WhitespaceAnalyzer();
                System.out.println("Selected Analyzer for Index Writer is: WhitespaceAnalyzer()");
                break;
            default:
                analyzer = new EnglishAnalyzer(EnglishAnalyzer.getDefaultStopSet());
                System.out.println("Selected Default Analyzer for Index Writer: EnglishAnalyzer()");
        }

        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter iwriter = new IndexWriter(directory, config);

        String filePath = "../cran.all.1400";
        String text = Utils.readFullFile(filePath);
        int index = -1;
        int count = 0;

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

        Document document = null;
        for (int i = 1; i < indexData.size(); i++) {
            String midText = text.substring(indexData.get(i - 1), indexData.get(i));
            String index_doc = midText.substring(midText.indexOf(".I") + ".I\n".length(), midText.indexOf(".T")).trim();
            String title_doc = midText.substring(midText.indexOf(".T") + ".T\n".length(), midText.indexOf(".A")).trim();
            String author_doc = midText.substring(midText.indexOf(".A") + ".A\n".length(), midText.indexOf(".B")).trim();
            String biblio_doc = midText.substring(midText.indexOf(".B") + ".B\n".length(), midText.indexOf(".W")).trim();
            String word_doc = midText.substring(midText.indexOf(".W") + ".W\n".length(), midText.length()).trim();

            document = new Document();
            document.add(new StringField("docid", index_doc, Field.Store.YES));
            document.add(new TextField("title", title_doc, Field.Store.YES));
            document.add(new TextField("author", author_doc, Field.Store.YES));
            document.add(new TextField("biblio", biblio_doc, Field.Store.YES));
            document.add(new TextField("words", word_doc, Field.Store.YES));
            iwriter.addDocument(document);

        }
        iwriter.close();
        directory.close();

        Query.searcher(null);

    }
}
