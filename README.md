# 🔍 Search Engine from Scratch

![CI Status](https://github.com/SinghCodesz/search-engine/actions/workflows/ci.yml/badge.svg)
![Deployment](https://img.shields.io/badge/deployed-Render-46e3b7?logo=render)
![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6db33f?logo=springboot)

A full-stack web search engine built entirely from scratch in Java — no Elasticsearch, no Lucene, no search libraries. Crawls web pages, builds an inverted index with positional information, and ranks results using TF-IDF and BM25.

## 🎥 Live Demo

**[🔗 Try it live](https://search-engine-hh9y.onrender.com)** — Search through 50+ crawled and indexed web pages.

> ⚠️ Free tier sleeps after 15 minutes of inactivity. First request may take 30-50 seconds to wake up.

## ✨ Features

### Web Crawling
- **Multi-threaded BFS crawler** — 5 parallel threads with configurable politeness delays
- **robots.txt compliance** — Respects crawl directives
- **URL deduplication** — HashSet-based visited URL tracking
- **Domain-level rate limiting** — 500ms delay between requests to same domain
- **Throughput:** 3 pages/second (50 pages in 16 seconds)

### Text Processing Pipeline
- **Tokenizer** — Splits text on non-alphabetic characters, lowercases
- **Stop Word Filter** — Removes 150+ English stop words
- **Porter Stemmer** — Custom implementation reducing words to root form
  - "running" → "run", "foxes" → "fox"

### Inverted Index
- **HashMap-based** — O(1) term lookup
- **Positional information** — Stores exact word positions for phrase queries
- **Disk serialization** — 3-file binary format survives restarts
- **615 unique terms** indexed from 50 documents

### Ranking
- **TF-IDF** — Term Frequency × Inverse Document Frequency
- **BM25** — Industry-standard ranking with term saturation and document length normalization
  - k1=1.2, b=0.75
  - 100 occurrences is only ~2x better than 10 (realistic relevance)

### Query Engine
- **Multi-word AND/OR queries** — Two-pointer postings intersection in O(n+m)
- **Phrase queries** — "machine learning" finds adjacent terms using positional index
- **Spell correction** — Levenshtein edit distance with "Did you mean?" suggestions
- **Autocomplete** — Trie-based prefix search with frequency ranking
- **Snippet generator** — Contextual text preview around query terms with highlighting
- **Pagination** — 10 results per page with full page number navigation

### Web Interface
- **Google-style search UI** — Clean, responsive design
- **Real-time autocomplete dropdown** — Keyboard navigation (arrows, Enter, Escape)
- **Spell correction display** — Clickable "Did you mean?" links
- **Relevance scores** — Transparent ranking with BM25/TF-IDF scores shown

## 🏗 Architecture
┌─────────────────────────────────────────────────────────────────────┐
│ SEARCH ENGINE PIPELINE │
├─────────────────────────────────────────────────────────────────────┤
│ │
│ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │
│ │ CRAWL │───→│ INDEX │───→│ QUERY │───→│ RANK & │ │
│ │ │ │ │ │ │ │ RETURN │ │
│ └──────────┘ └──────────┘ └──────────┘ └──────────┘ │
│ │ │ │ │ │
│ ▼ ▼ ▼ ▼ │
│ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │
│ │ BFS │ │ Inverted │ │ AND/OR │ │ TF-IDF │ │
│ │ Frontier │ │ Index │ │ Intersect│ │ + BM25 │ │
│ │ (Queue) │ │ (HashMap)│ │ (2-ptr) │ │ Ranking │ │
│ └──────────┘ └──────────┘ └──────────┘ └──────────┘ │
│ │ │ │ │ │
│ ▼ ▼ ▼ ▼ │
│ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │
│ │ 5 Threads│ │ Positional│ │ Phrase │ │ Snippets │ │
│ │ Politeness│ │ Index │ │ Queries │ │ + Pagin- │ │
│ │ robots.txt│ │ (Disk) │ │ (Adjacent)│ │ ation │ │
│ └──────────┘ └──────────┘ └──────────┘ └──────────┘ │
│ │
│ ┌──────────────────────────────────────────────────────────────┐ │
│ │ ADDITIONAL FEATURES │ │
│ │ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │ │
│ │ │ Spell │ │ Auto- │ │ Web │ │ Spring │ │ │
│ │ │ Check │ │ complete│ │ UI │ │ Boot │ │ │
│ │ │(Levensh- │ │ (Trie) │ │(Thymeleaf│ │ Server │ │ │
│ │ │ tein) │ │ │ │ + CSS) │ │ │ │ │
│ │ └──────────┘ └──────────┘ └──────────┘ └──────────┘ │ │
│ └──────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘

### Search Flow
User enters: "machine learning"
│
▼

1. Query Parser: tokenize → stop words → stem
   ["machin", "learn"]
     │
     ▼

2. Check Phrase: Is it quoted? → NO
     │
     ▼

3. Spell Check: Are these in dictionary? → YES
     │
     ▼

4. Retrieve Postings:
   "machin" → [doc3, doc7, doc12]
   "learn" → [doc1, doc3, doc7, doc9]
     │
     ▼

5. Intersect (AND):
   Two-pointer merge → [doc3, doc7]
     │
     ▼

6. Rank (BM25):
   doc3: score(term1) + score(term2) = 3.45 + 2.10 = 5.55
   doc7: score(term1) + score(term2) = 1.20 + 4.30 = 5.50
     │
     ▼

7. Generate Snippets:
   doc3: "...different types of machine learning algorithms..."
   doc7: "...the machine is capable of learning from data..."
     │
     ▼

8. Return Results (Top 10, paginated):
   #1: doc3 (5.55) — "Introduction to Machine Learning"
   #2: doc7 (5.50) — "AI and Machine Learning Basics"


## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.5 (web + Thymeleaf) |
| **HTML Parser** | JSoup 1.17 |
| **Concurrency** | java.util.concurrent (ExecutorService, ConcurrentHashMap) |
| **Index** | Custom HashMap-based inverted index with binary serialization |
| **Ranking** | TF-IDF + BM25 (Okapi) |
| **Spell Check** | Levenshtein edit distance (DP algorithm) |
| **Autocomplete** | Custom Trie data structure |
| **Frontend** | Thymeleaf, vanilla JavaScript, CSS |
| **DevOps** | Docker, GitHub Actions, Render Cloud |

## 📁 Project Structure
src/main/java/com/searchengine/
├── crawler/
│ ├── WebCrawler.java — Single-page fetcher
│ ├── CrawlFrontier.java — BFS URL queue + visited set
│ └── MultiThreadedCrawler.java — Thread pool manager
├── indexer/
│ ├── InvertedIndex.java — HashMap-based index
│ ├── Posting.java — Document + frequency + positions
│ └── IndexSerializer.java — Binary disk persistence
├── query/
│ ├── QueryParser.java — Query tokenization
│ ├── PostingsIntersector.java — Two-pointer AND/OR merge
│ ├── PhraseQueryHandler.java — Adjacent term matching
│ ├── SpellCorrector.java — Levenshtein distance
│ ├── SnippetGenerator.java — Contextual text extraction
│ └── SearchService.java — Full search pipeline coordinator
├── ranker/
│ ├── Ranker.java — Ranking interface
│ ├── DocumentScore.java — Doc ID + score model
│ ├── TFIDFRanker.java — TF-IDF implementation
│ └── BM25Ranker.java — BM25 (Okapi) implementation
├── text/
│ ├── Tokenizer.java — Text → tokens
│ ├── StopWordFilter.java — Remove 150+ stop words
│ ├── PorterStemmer.java — Stemming algorithm
│ └── TextProcessor.java — Full pipeline
├── model/
│ └── Document.java — URL, title, text, links
├── web/
│ └── SearchController.java — Spring MVC controller
└── SearchEngineApplication.java — Main class


## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+

### Run Locally

```bash
# Clone the repository
git clone https://github.com/SinghCodesz/search-engine.git
cd search-engine

# Run (loads pre-built index from data/ folder)
mvn spring-boot:run

# Open in browser
open http://localhost:9000
```
### Run with Docker
docker build -t search-engine .
docker run -p 9000:9000 search-engine

### Rebuild the Index
#### Crawl fresh pages and rebuild index
mvn exec:java -Dexec.mainClass="com.searchengine.Main"
## 📊 Performance
Metric	Value
Pages crawled	50 (designed for 100K+)
Crawl throughput	3 pages/sec (5 threads)
Unique terms indexed	615
Index build time	< 1 second (from scratch)
Index load time	< 100ms (from disk)
Query response	< 500ms (multi-word AND)
Spell check	< 50ms per word
## 🔮 Future Enhancements
PageRank integration for link-based authority

Vector embeddings for semantic search

RAG pipeline (Retrieval-Augmented Generation)

Distributed index across multiple servers

Real-time indexing (stream new pages)

User search history and personalization

## 📄 License
MIT — feel free to use this code for your own projects.

Built with ❤️ by Arjun Singh