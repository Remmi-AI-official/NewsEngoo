package com.example.data.seed

import com.example.data.local.AppDatabase
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.DailyProgressEntity
import com.example.data.local.entity.EditorialEntity
import com.example.data.local.entity.EditorialVocabularyCrossRef
import com.example.data.local.entity.GrammarRuleEntity
import com.example.data.local.entity.PhraseEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.TestEntity
import com.example.data.local.entity.TestQuestionCrossRef
import com.example.data.local.entity.VocabularyEntity
import com.example.data.local.entity.WordCategoryCrossRef
import com.example.domain.model.DateUtils
import com.example.domain.model.WordLearningStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SeedDataLoader {

    suspend fun seedInitialDataIfEmpty(database: AppDatabase) = withContext(Dispatchers.IO) {
        val editorialDao = database.editorialDao()
        val vocabDao = database.vocabularyDao()
        val grammarDao = database.grammarDao()
        val phraseDao = database.phraseDao()
        val testDao = database.practiceTestDao()
        val progressDao = database.dailyProgressDao()

        // Check if already seeded
        val existingEditorial = editorialDao.getEditorialByDateSync("2026-10-04")
        if (existingEditorial != null) return@withContext

        // 1. Categories
        val defaultCategories = listOf(
            CategoryEntity("cat_economy", "Economy & Markets", "Fiscal policy, trade, inflation, macroeconomics", "#1A365D", true),
            CategoryEntity("cat_upsc", "UPSC & Civil Services", "High-frequency analytical terms for competitive exams", "#7B1E30", true),
            CategoryEntity("cat_politics", "Governance & Polity", "Constitutional, legislative, and foreign affairs", "#22543D", true),
            CategoryEntity("cat_difficult", "Difficult Words", "Rare, academic, and GRE/editorial level terms", "#C05621", true),
            CategoryEntity("cat_idioms", "Editorial Idioms", "Metaphors and collocations used by columnists", "#4A5568", true)
        )
        for (cat in defaultCategories) {
            vocabDao.insertCategory(cat)
        }

        // 2. Today's Editorial (2026-10-04)
        val todayEditorial = EditorialEntity(
            id = "ed_2026-10-04",
            date = "2026-10-04",
            title = "The Changing Nature of India's Economy: Balancing Growth and Equity",
            source = "The Hindu Editorial",
            readTimeMinutes = 6,
            contentMarkdown = """# The Changing Nature of India's Economy: Balancing Growth and Equity

*Source: The Hindu Editorial | Published: 04 October 2026*

India's economic trajectory in recent quarters presents a fascinating **dichotomy**. On one hand, headline GDP growth rates continue to outpace most major global peers, driven by resilient domestic consumption and sustained infrastructure spending. On the other hand, the benefits of this expansion remain unevenly distributed, raising concerns about a **nascent** k-shaped recovery that risks leaving informal workers behind.

### A Structural Shift in Manufacturing

The government's **pragmatic** push towards electronics manufacturing and renewable energy infrastructure has provided much-needed **impetus** to industrial capital expenditure. Global conglomerates seeking to diversify their supply chains away from single-source vulnerabilities have found India an attractive destination. However, this capital-intensive model has not yet yielded the **ubiquitous** job creation required to absorb millions of young entrants joining the labor market annually.

To **mitigate** these structural disparities, policymakers must **bolster** labor-intensive segments such as textiles, food processing, and rural artisanal manufacturing. While high-tech investments represent a **watershed** moment for technological sovereignty, neglecting basic manufacturing could **exacerbate** urban-rural wage gaps.

### The Fiscal Balancing Act

The monetary authorities face an **austere** dilemma. While inflationary pressures have moderated, volatile food and energy costs remain a **tenuous** anchor. A hasty easing of borrowing rates could jeopardize hard-won macroeconomic stability, whereas an excessively tight monetary stance may suppress private investment.

Ultimately, economic progress cannot be sustained through financial engineering alone. A systemic shift toward skill enhancement, female labor force participation, and institutional transparency will decide whether the current momentum is a **transient** boom or an enduring **paradigm** of inclusive prosperity.
""".trimIndent()
        )
        editorialDao.insertEditorial(todayEditorial)

        // 3. 18 Vocabulary Words for 2026-10-04
        val vocabList = listOf(
            VocabularyEntity(
                id = "pragmatic",
                word = "Pragmatic",
                pronunciation = "/præɡˈmætɪk/",
                partOfSpeech = "adjective",
                meaning = "Dealing with things sensibly and realistically based on practical rather than theoretical considerations.",
                hindiMeaning = "व्यावहारिक",
                synonyms = listOf("practical", "realistic", "sensible", "utilitarian"),
                antonyms = listOf("idealistic", "impractical", "visionary"),
                exampleSentence = "The government adopted a pragmatic approach to industrial incentives.",
                wordFamily = "pragmatism (n), pragmatically (adv)",
                difficulty = "medium",
                sourceArticle = "The Changing Nature of India's Economy",
                learningStatus = WordLearningStatus.NEW.name,
                nextReviewDate = "2026-10-05"
            ),
            VocabularyEntity(
                id = "dichotomy",
                word = "Dichotomy",
                pronunciation = "/daɪˈkɒtəmi/",
                partOfSpeech = "noun",
                meaning = "A division or contrast between two things that are represented as being opposed or entirely different.",
                hindiMeaning = "द्विभाजन, विरोधाभास",
                synonyms = listOf("division", "separation", "contrast", "polarity"),
                antonyms = listOf("harmony", "unity", "similarity"),
                exampleSentence = "The editorial highlights the dichotomy between roaring stock markets and stagnant rural wages.",
                wordFamily = "dichotomous (adj)",
                difficulty = "hard",
                sourceArticle = "The Changing Nature of India's Economy",
                learningStatus = WordLearningStatus.NEW.name,
                nextReviewDate = "2026-10-05"
            ),
            VocabularyEntity(
                id = "impetus",
                word = "Impetus",
                pronunciation = "/ˈɪmpɪtəs/",
                partOfSpeech = "noun",
                meaning = "The force or energy with which a body moves; a driving force or stimulation.",
                hindiMeaning = "प्रोत्साहन, गति",
                synonyms = listOf("momentum", "stimulus", "catalyst", "incentive"),
                antonyms = listOf("hindrance", "deterrent", "obstacle"),
                exampleSentence = "Public capital expenditure provided a significant impetus to the capital goods sector.",
                wordFamily = "impetuous (adj)",
                difficulty = "medium",
                sourceArticle = "The Changing Nature of India's Economy",
                learningStatus = WordLearningStatus.NEW.name,
                nextReviewDate = "2026-10-05"
            ),
            VocabularyEntity(
                id = "ubiquitous",
                word = "Ubiquitous",
                pronunciation = "/juːˈbɪkwɪtəs/",
                partOfSpeech = "adjective",
                meaning = "Present, appearing, or found everywhere simultaneously.",
                hindiMeaning = "सर्वव्यापी",
                synonyms = listOf("omnipresent", "pervasive", "universal", "widespread"),
                antonyms = listOf("rare", "scarce", "localized"),
                exampleSentence = "Smartphones and digital payments have become ubiquitous across Indian towns.",
                wordFamily = "ubiquity (n), ubiquitously (adv)",
                difficulty = "medium",
                sourceArticle = "The Changing Nature of India's Economy",
                learningStatus = WordLearningStatus.NEW.name,
                nextReviewDate = "2026-10-05"
            ),
            VocabularyEntity(
                id = "mitigate",
                word = "Mitigate",
                pronunciation = "/ˈmɪtɪɡeɪt/",
                partOfSpeech = "verb",
                meaning = "Make something bad less severe, serious, or painful.",
                hindiMeaning = "कम करना, शांत करना",
                synonyms = listOf("alleviate", "lessen", "reduce", "diminish"),
                antonyms = listOf("aggravate", "exacerbate", "worsen"),
                exampleSentence = "Targeted welfare transfers helped mitigate the impact of rising food inflation.",
                wordFamily = "mitigation (n), mitigating (adj)",
                difficulty = "medium",
                sourceArticle = "The Changing Nature of India's Economy",
                learningStatus = WordLearningStatus.NEW.name,
                nextReviewDate = "2026-10-05"
            ),
            VocabularyEntity(
                id = "bolster",
                word = "Bolster",
                pronunciation = "/ˈbəʊlstə/",
                partOfSpeech = "verb",
                meaning = "Support or strengthen; prop up.",
                hindiMeaning = "मजबूत करना, सहारा देना",
                synonyms = listOf("strengthen", "reinforce", "boost", "sustain"),
                antonyms = listOf("undermine", "weaken", "impair"),
                exampleSentence = "The central bank acted decisively to bolster foreign exchange reserves.",
                wordFamily = "bolstered (past)",
                difficulty = "easy",
                sourceArticle = "The Changing Nature of India's Economy",
                learningStatus = WordLearningStatus.NEW.name,
                nextReviewDate = "2026-10-05"
            ),
            VocabularyEntity(
                id = "watershed",
                word = "Watershed",
                pronunciation = "/ˈwɔːtəʃɛd/",
                partOfSpeech = "noun",
                meaning = "An event or period marking a turning point in a course of action or state of affairs.",
                hindiMeaning = "निर्णायक मोड़, ऐतिहासिक मोड़",
                synonyms = listOf("turning point", "milestone", "landmark", "crossroads"),
                antonyms = listOf("triviality", "unremarkable event"),
                exampleSentence = "The 1991 economic reforms represented a watershed moment in contemporary Indian history.",
                wordFamily = "watershed (adj)",
                difficulty = "medium",
                sourceArticle = "The Changing Nature of India's Economy",
                learningStatus = WordLearningStatus.NEW.name,
                nextReviewDate = "2026-10-05"
            ),
            VocabularyEntity(
                id = "exacerbate",
                word = "Exacerbate",
                pronunciation = "/ɪɡˈzæsəbeɪt/",
                partOfSpeech = "verb",
                meaning = "Make a problem, bad situation, or negative feeling worse.",
                hindiMeaning = "बिगाड़ना, गंभीर बनाना",
                synonyms = listOf("worsen", "aggravate", "intensify", "inflame"),
                antonyms = listOf("ameliorate", "soothe", "mitigate"),
                exampleSentence = "Supply bottlenecks could exacerbate existing retail inflation.",
                wordFamily = "exacerbation (n)",
                difficulty = "hard",
                sourceArticle = "The Changing Nature of India's Economy",
                learningStatus = WordLearningStatus.NEW.name,
                nextReviewDate = "2026-10-05"
            ),
            VocabularyEntity(
                id = "austere",
                word = "Austere",
                pronunciation = "/ɒˈstɪə/",
                partOfSpeech = "adjective",
                meaning = "Severe or strict in manner, attitude, or appearance; having no comforts or luxuries.",
                hindiMeaning = "कठोर, सादगीपूर्ण",
                synonyms = listOf("severe", "strict", "frugal", "unadorned"),
                antonyms = listOf("luxurious", "indulgent", "lenient"),
                exampleSentence = "The finance ministry adopted an austere fiscal posture to rein in deficits.",
                wordFamily = "austerity (n), austerely (adv)",
                difficulty = "hard",
                sourceArticle = "The Changing Nature of India's Economy",
                learningStatus = WordLearningStatus.NEW.name,
                nextReviewDate = "2026-10-05"
            ),
            VocabularyEntity(
                id = "tenuous",
                word = "Tenuous",
                pronunciation = "/ˈtɛnjuəs/",
                partOfSpeech = "adjective",
                meaning = "Very weak or slight; insubstantial.",
                hindiMeaning = "कमजोर, अनिश्चित",
                synonyms = listOf("fragile", "shaky", "flimsy", "doubtful"),
                antonyms = listOf("robust", "strong", "solid"),
                exampleSentence = "The ceasefire agreement remains tenuous amid periodic border tensions.",
                wordFamily = "tenuously (adv), tenuousness (n)",
                difficulty = "hard",
                sourceArticle = "The Changing Nature of India's Economy",
                learningStatus = WordLearningStatus.NEW.name,
                nextReviewDate = "2026-10-05"
            ),
            VocabularyEntity(
                id = "transient",
                word = "Transient",
                pronunciation = "/ˈtrænziənt/",
                partOfSpeech = "adjective",
                meaning = "Lasting only for a short time; impermanent.",
                hindiMeaning = "क्षणिक, अस्थायी",
                synonyms = listOf("temporary", "fleeting", "ephemeral", "short-lived"),
                antonyms = listOf("permanent", "enduring", "perpetual"),
                exampleSentence = "Central bankers initially misjudged the spike in commodity prices as transient.",
                wordFamily = "transience (n), transiently (adv)",
                difficulty = "medium",
                sourceArticle = "The Changing Nature of India's Economy",
                learningStatus = WordLearningStatus.NEW.name,
                nextReviewDate = "2026-10-05"
            ),
            VocabularyEntity(
                id = "paradigm",
                word = "Paradigm",
                pronunciation = "/ˈpærədaɪm/",
                partOfSpeech = "noun",
                meaning = "A typical example or pattern of something; a distinct set of concepts or thought patterns.",
                hindiMeaning = "प्रतिमान, मिसाल",
                synonyms = listOf("model", "archetype", "framework", "standard"),
                antonyms = listOf("aberration", "anomaly"),
                exampleSentence = "Renewable energy adoption marks a paradigm shift in national power generation.",
                wordFamily = "paradigmatic (adj)",
                difficulty = "medium",
                sourceArticle = "The Changing Nature of India's Economy",
                learningStatus = WordLearningStatus.NEW.name,
                nextReviewDate = "2026-10-05"
            ),
            VocabularyEntity(
                id = "nascent",
                word = "Nascent",
                pronunciation = "/ˈnæsnt/",
                partOfSpeech = "adjective",
                meaning = "Just coming into existence and beginning to display signs of future potential.",
                hindiMeaning = "उभरता हुआ, नवजात",
                synonyms = listOf("emerging", "budding", "developing", "incipient"),
                antonyms = listOf("mature", "dying", "fading"),
                exampleSentence = "Venture capital funding has provided oxygen to the nascent clean-tech startup ecosystem.",
                wordFamily = "nascence (n)",
                difficulty = "hard",
                sourceArticle = "The Changing Nature of India's Economy",
                learningStatus = WordLearningStatus.NEW.name,
                nextReviewDate = "2026-10-05"
            ),
            VocabularyEntity(
                id = "resilient",
                word = "Resilient",
                pronunciation = "/rɪˈzɪliənt/",
                partOfSpeech = "adjective",
                meaning = "Able to withstand or recover quickly from difficult conditions.",
                hindiMeaning = "लचीला, सहनशील",
                synonyms = listOf("robust", "tough", "durable", "buoyant"),
                antonyms = listOf("vulnerable", "fragile", "weak"),
                exampleSentence = "The banking sector proved surprisingly resilient in the wake of international banking tremors.",
                wordFamily = "resilience (n), resiliently (adv)",
                difficulty = "easy",
                sourceArticle = "The Changing Nature of India's Economy",
                learningStatus = WordLearningStatus.NEW.name,
                nextReviewDate = "2026-10-05"
            ),
            VocabularyEntity(
                id = "pervasive",
                word = "Pervasive",
                pronunciation = "/pəˈveɪsɪv/",
                partOfSpeech = "adjective",
                meaning = "Spreading widely throughout an area or a group of people, especially an unwelcome influence.",
                hindiMeaning = "व्यापक, सर्वप्रभावी",
                synonyms = listOf("prevalent", "extensive", "permeating", "ubiquitous"),
                antonyms = listOf("isolated", "confined", "rare"),
                exampleSentence = "Corruption created a pervasive sense of cynicism among prospective entrepreneurs.",
                wordFamily = "pervade (v), pervasiveness (n)",
                difficulty = "medium",
                sourceArticle = "The Changing Nature of India's Economy",
                learningStatus = WordLearningStatus.NEW.name,
                nextReviewDate = "2026-10-05"
            ),
            VocabularyEntity(
                id = "coalesce",
                word = "Coalesce",
                pronunciation = "/ˌkəʊəˈlɛs/",
                partOfSpeech = "verb",
                meaning = "Come together to form one mass or whole.",
                hindiMeaning = "संयुक्त होना, घुल-मिल जाना",
                synonyms = listOf("merge", "unite", "fuse", "amalgamate"),
                antonyms = listOf("disperse", "separate", "split"),
                exampleSentence = "Several small opposition parties sought to coalesce around a common economic manifesto.",
                wordFamily = "coalescence (n), coalescent (adj)",
                difficulty = "hard",
                sourceArticle = "The Changing Nature of India's Economy",
                learningStatus = WordLearningStatus.NEW.name,
                nextReviewDate = "2026-10-05"
            ),
            VocabularyEntity(
                id = "incumbent",
                word = "Incumbent",
                pronunciation = "/ɪnˈkʌmbənt/",
                partOfSpeech = "adjective",
                meaning = "Necessary for someone as a duty or responsibility; also holding an indicated position.",
                hindiMeaning = "वर्तमान पदधारी; कर्तव्य",
                synonyms = listOf("obligatory", "mandatory", "reigning", "current"),
                antonyms = listOf("optional", "challenger"),
                exampleSentence = "It is incumbent upon public leaders to maintain fiscal discipline during election years.",
                wordFamily = "incumbency (n)",
                difficulty = "medium",
                sourceArticle = "The Changing Nature of India's Economy",
                learningStatus = WordLearningStatus.NEW.name,
                nextReviewDate = "2026-10-05"
            ),
            VocabularyEntity(
                id = "esoteric",
                word = "Esoteric",
                pronunciation = "/ˌɛsəˈtɛrɪk/",
                partOfSpeech = "adjective",
                meaning = "Intended for or likely to be understood by only a small number of people with specialized knowledge.",
                hindiMeaning = "गूढ़, गुप्त",
                synonyms = listOf("obscure", "abstruse", "arcane", "recondite"),
                antonyms = listOf("simple", "common", "transparent"),
                exampleSentence = "Complex financial derivatives often hide risks behind esoteric mathematical models.",
                wordFamily = "esoterically (adv), esotericism (n)",
                difficulty = "hard",
                sourceArticle = "The Changing Nature of India's Economy",
                learningStatus = WordLearningStatus.NEW.name,
                nextReviewDate = "2026-10-05"
            )
        )

        for (v in vocabList) {
            vocabDao.insertWord(v)
            vocabDao.linkWordToEditorial(
                EditorialVocabularyCrossRef(
                    editorialId = "ed_2026-10-04",
                    wordId = v.id,
                    date = "2026-10-04"
                )
            )
            // Categorize
            if (v.id in listOf("pragmatic", "dichotomy", "impetus", "bolster", "tenuous", "paradigm")) {
                vocabDao.linkWordToCategory(WordCategoryCrossRef(v.id, "cat_economy"))
            }
            if (v.id in listOf("dichotomy", "exacerbate", "nascent", "coalesce", "esoteric", "austere")) {
                vocabDao.linkWordToCategory(WordCategoryCrossRef(v.id, "cat_upsc"))
                vocabDao.linkWordToCategory(WordCategoryCrossRef(v.id, "cat_difficult"))
            }
        }

        // 4. Grammar Rules (4 Rules for 2026-10-04)
        val grammarRules = listOf(
            GrammarRuleEntity(
                id = "rule_2026-10-04_despite_although",
                date = "2026-10-04",
                title = "Despite / In Spite Of vs. Although / Even Though",
                rule = "'Despite' and 'in spite of' are prepositions followed by a noun, noun phrase, or gerund (-ing). 'Although', 'even though', and 'though' are conjunctions followed by a full clause (subject + verb).",
                explanation = "A common editorial error is writing 'Despite he was tired' or using 'of' with despite ('despite of'). Always remember: 'despite' never takes 'of'. If you want to use a clause after 'despite', you must insert 'the fact that' (e.g., 'despite the fact that he was tired').",
                correctExamples = listOf(
                    "Despite facing persistent inflation, consumer spending held steady.",
                    "In spite of the economic headwinds, exports grew by four percent.",
                    "Although the government initiated tax reforms, collection remained sluggish."
                ),
                incorrectExamples = listOf(
                    "Despite of high borrowing rates, banks expanded lending.",
                    "Despite he worked late, the project fell behind schedule."
                ),
                commonMistakes = "Writing 'despite of' instead of 'in spite of', or attaching a full independent clause directly to 'despite'.",
                editorialExample = "Despite global monetary tightening, India's sovereign debt market remained orderly.",
                difficulty = "medium",
                tags = listOf("conjunctions", "prepositions", "sentence-correction")
            ),
            GrammarRuleEntity(
                id = "rule_2026-10-04_parallelism",
                date = "2026-10-04",
                title = "Correlative Conjunctions & Parallel Structure (Not only... but also)",
                rule = "When using correlative conjunctions like 'not only... but also', 'either... or', or 'neither... nor', the grammatical structures that follow both parts must be strictly parallel.",
                explanation = "If 'not only' is followed by a verb phrase, 'but also' must be followed by a verb phrase. If 'not only' is followed by a prepositional phrase, 'but also' must mirror it.",
                correctExamples = listOf(
                    "The policy not only curtailed fiscal leakage but also encouraged formal compliance.",
                    "He was known not only for his intellect but also for his humility."
                ),
                incorrectExamples = listOf(
                    "The policy not only curtailed fiscal leakage, but it also was encouraging to small traders."
                ),
                commonMistakes = "Placing 'not only' before the subject or verb while placing 'but also' before a noun, breaking grammatical parallelism.",
                editorialExample = "The stimulus package aimed not only to revive rural demand but also to stabilize industrial supply.",
                difficulty = "medium",
                tags = listOf("parallelism", "syntax", "sentence-correction")
            ),
            GrammarRuleEntity(
                id = "rule_2026-10-04_subject_verb_inversion",
                date = "2026-10-04",
                title = "Subject-Verb Agreement in Inverted Sentences",
                rule = "In sentences beginning with negative adverbs or prepositional phrases of location (e.g., 'Seldom', 'Rarely', 'Under no circumstances', 'Among the proposals is/are...'), the subject follows the verb. The verb must agree with that delayed subject.",
                explanation = "Writers frequently match the verb with the nearest preceding noun instead of the actual delayed subject.",
                correctExamples = listOf(
                    "Rarely has the central bank encountered such volatile commodity swings.",
                    "Among the key priorities outlined by the commission are healthcare and education."
                ),
                incorrectExamples = listOf(
                    "Among the key priorities outlined by the commission is healthcare and education."
                ),
                commonMistakes = "Treating the introductory adverbial phrase as the subject.",
                editorialExample = "Scarcely had the quarter's trade figures been published when foreign portfolio flows reversed.",
                difficulty = "hard",
                tags = listOf("subject-verb-agreement", "inversion", "advanced-grammar")
            ),
            GrammarRuleEntity(
                id = "rule_2026-10-04_dangling_modifiers",
                date = "2026-10-04",
                title = "Dangling Modifiers and Participial Clauses",
                rule = "An introductory participial phrase must modify the subject of the main clause immediately following it.",
                explanation = "If the actor performing the action in the participial phrase is not the subject of the main clause, the modifier is 'dangling', creating unintended comic or nonsensical meanings.",
                correctExamples = listOf(
                    "Having scrutinized the balance sheet, the auditor issued an unqualified opinion.",
                    "While reviewing the industrial index, economists noticed a sharp divergence."
                ),
                incorrectExamples = listOf(
                    "Having scrutinized the balance sheet, multiple discrepancies were discovered by the auditor."
                ),
                commonMistakes = "Having the participial clause point to an inanimate object that cannot perform the action.",
                editorialExample = "Facing escalating raw material costs, manufacturers opted to pass on higher prices to consumers.",
                difficulty = "hard",
                tags = listOf("modifiers", "participles", "clarity")
            )
        )
        grammarDao.insertRules(grammarRules)

        // 5. Editorial Phrases / Expressions (7 items)
        val phrases = listOf(
            PhraseEntity(
                id = "phrase_2026-10-04_bite_the_bullet",
                date = "2026-10-04",
                phrase = "Bite the bullet",
                type = "idiom",
                meaning = "Decide to do something difficult or unpleasant that one has been putting off or hesitating over.",
                hindiMeaning = "मुसीबत का डटकर सामना करना; अप्रिय फैसला लेना",
                example = "The ministry had to bite the bullet and rationalize non-merit power subsidies.",
                category = "Editorial Idioms",
                difficulty = "medium",
                source = "The Hindu Editorial"
            ),
            PhraseEntity(
                id = "phrase_2026-10-04_double_edged_sword",
                date = "2026-10-04",
                phrase = "Double-edged sword",
                type = "metaphor",
                meaning = "A situation, action, or decision that has both positive and negative consequences.",
                hindiMeaning = "दोधारी तलवार",
                example = "Rapid currency depreciation is a double-edged sword: it helps exporters but escalates oil import bills.",
                category = "Economy & Markets",
                difficulty = "easy",
                source = "The Hindu Editorial"
            ),
            PhraseEntity(
                id = "phrase_2026-10-04_at_a_crossroads",
                date = "2026-10-04",
                phrase = "At a crossroads",
                type = "collocation",
                meaning = "At a point in life or history where an important choice or decisive change must be made.",
                hindiMeaning = "निर्णायक मोड़ पर",
                example = "India's agricultural policy stands at a crossroads between price guarantees and market liberalization.",
                category = "Governance & Polity",
                difficulty = "easy",
                source = "The Hindu Editorial"
            ),
            PhraseEntity(
                id = "phrase_2026-10-04_ring_fence",
                date = "2026-10-04",
                phrase = "Ring-fence",
                type = "formal_expression",
                meaning = "Guarantee that a given sum of money or resource will be used solely for a specified project or purpose.",
                hindiMeaning = "सुरक्षित रखना, निर्धारित उद्देश्य के लिए अलग रखना",
                example = "Parliament voted to ring-fence social welfare expenditures from across-the-board budget cuts.",
                category = "Economy & Markets",
                difficulty = "hard",
                source = "The Hindu Editorial"
            ),
            PhraseEntity(
                id = "phrase_2026-10-04_sea_change",
                date = "2026-10-04",
                phrase = "Sea change",
                type = "idiom",
                meaning = "A profound or marked transformation in perspective, policy, or character.",
                hindiMeaning = "युगांतरकारी परिवर्तन, आमूलचूल बदलाव",
                example = "The transition to electric mobility represents a sea change for the automotive manufacturing value chain.",
                category = "Editorial Idioms",
                difficulty = "medium",
                source = "The Hindu Editorial"
            ),
            PhraseEntity(
                id = "phrase_2026-10-04_silver_bullet",
                date = "2026-10-04",
                phrase = "Silver bullet",
                type = "idiom",
                meaning = "A simple, magical solution to a complicated problem.",
                hindiMeaning = "रामबाण इलाज",
                example = "Subsidies are not a silver bullet for addressing chronic structural underemployment.",
                category = "General",
                difficulty = "easy",
                source = "The Hindu Editorial"
            ),
            PhraseEntity(
                id = "phrase_2026-10-04_call_a_spade_a_spade",
                date = "2026-10-04",
                phrase = "Call a spade a spade",
                type = "idiom",
                meaning = "Speak frankly and directly about a problem or reality, even if it is unpleasant.",
                hindiMeaning = "साफ-साफ बात कहना, खरी-खरी सुनाना",
                example = "The editorial called a spade a spade, acknowledging that administrative inertia had stalled urban infrastructure.",
                category = "Spoken English",
                difficulty = "medium",
                source = "The Hindu Editorial"
            )
        )
        phraseDao.insertPhrases(phrases)

        // 6. Practice Questions (15 Questions for 2026-10-04)
        val questions = listOf(
            QuestionEntity(
                id = "q_2026-10-04_01",
                date = "2026-10-04",
                testId = "daily_2026-10-04",
                type = "mcq",
                question = "In the context of the editorial, what is the best synonym for 'PRAGMATIC'?",
                options = listOf("Idealistic", "Realistic and practical", "Uncompromising", "Dogmatic"),
                correctAnswerIndex = 1,
                explanation = "'Pragmatic' refers to dealing with situations practically and realistically rather than theoretically.",
                topic = "vocabulary",
                difficulty = "medium",
                relatedWordOrRule = "pragmatic"
            ),
            QuestionEntity(
                id = "q_2026-10-04_02",
                date = "2026-10-04",
                testId = "daily_2026-10-04",
                type = "antonym",
                question = "Identify the ANTONYM of 'EXACERBATE':",
                options = listOf("Aggravate", "Intensify", "Ameliorate", "Perpetuate"),
                correctAnswerIndex = 2,
                explanation = "'Exacerbate' means to make a bad situation worse. 'Ameliorate' means to make something better or improve it.",
                topic = "vocabulary",
                difficulty = "hard",
                relatedWordOrRule = "exacerbate"
            ),
            QuestionEntity(
                id = "q_2026-10-04_03",
                date = "2026-10-04",
                testId = "daily_2026-10-04",
                type = "sentence_correction",
                question = "Select the grammatically CORRECT sentence:",
                options = listOf(
                    "Despite of high food inflation, consumer demand rose.",
                    "Despite high food inflation, consumer demand rose.",
                    "Despite he was facing inflation, consumer demand rose.",
                    "In despite of high food inflation, consumer demand rose."
                ),
                correctAnswerIndex = 1,
                explanation = "'Despite' takes a noun phrase directly without 'of'. 'Despite of' is incorrect; the equivalent phrase is 'In spite of'.",
                topic = "grammar",
                difficulty = "medium",
                relatedWordOrRule = "Despite vs. Although"
            ),
            QuestionEntity(
                id = "q_2026-10-04_04",
                date = "2026-10-04",
                testId = "daily_2026-10-04",
                type = "meaning",
                question = "What does the expression 'RING-FENCE' mean in financial governance?",
                options = listOf(
                    "To construct security boundaries around physical banks",
                    "To guarantee a fund or resource is allocated strictly for a specific purpose",
                    "To trade speculative derivatives across foreign markets",
                    "To dissolve bankrupt state enterprises"
                ),
                correctAnswerIndex = 1,
                explanation = "'Ring-fencing' involves legally or administratively protecting a sum of money or asset so that it cannot be diverted to other uses.",
                topic = "expressions",
                difficulty = "medium",
                relatedWordOrRule = "Ring-fence"
            ),
            QuestionEntity(
                id = "q_2026-10-04_05",
                date = "2026-10-04",
                testId = "daily_2026-10-04",
                type = "mcq",
                question = "Choose the word that best describes a 'turning point marking an era':",
                options = listOf("Transient", "Watershed", "Dichotomy", "Austere"),
                correctAnswerIndex = 1,
                explanation = "A 'watershed' event is a pivotal turning point that permanently shifts historical or policy trajectories.",
                topic = "vocabulary",
                difficulty = "medium",
                relatedWordOrRule = "watershed"
            ),
            QuestionEntity(
                id = "q_2026-10-04_06",
                date = "2026-10-04",
                testId = "daily_2026-10-04",
                type = "error_detection",
                question = "Find the error: 'The regulatory commission not only investigated (A) market cartels (B), but it also fined (C) non-compliant firms heavily (D).'",
                options = listOf(
                    "Part (A): investigated",
                    "Part (B): market cartels",
                    "Part (C): but it also fined",
                    "No error"
                ),
                correctAnswerIndex = 2,
                explanation = "Parallel structure requires 'not only investigated... but also fined'. Inserting the subject 'it' in Part C breaks parallel verb symmetry.",
                topic = "grammar",
                difficulty = "hard",
                relatedWordOrRule = "Correlative Conjunctions & Parallel Structure"
            ),
            QuestionEntity(
                id = "q_2026-10-04_07",
                date = "2026-10-04",
                testId = "daily_2026-10-04",
                type = "fill_blank",
                question = "The startups sought to _______ their diverse machine learning frameworks into a singular unified platform.",
                options = listOf("mitigate", "exacerbate", "coalesce", "attenuate"),
                correctAnswerIndex = 2,
                explanation = "'Coalesce' means to come together to form one mass, whole, or unified system.",
                topic = "vocabulary",
                difficulty = "hard",
                relatedWordOrRule = "coalesce"
            ),
            QuestionEntity(
                id = "q_2026-10-04_08",
                date = "2026-10-04",
                testId = "daily_2026-10-04",
                type = "mcq",
                question = "What is the primary dichotomy described in the opening paragraphs of today's editorial?",
                options = listOf(
                    "Fiscal stimulus vs. private equity inflows",
                    "High headline GDP growth vs. uneven distribution among workers",
                    "Digital currency rollout vs. cash hoarding",
                    "Agricultural surpluses vs. export restrictions"
                ),
                correctAnswerIndex = 1,
                explanation = "The editorial highlights the contrast between strong macroeconomic expansion and the unequal benefits reaching the informal sector.",
                topic = "comprehension",
                difficulty = "medium",
                relatedWordOrRule = "dichotomy"
            ),
            QuestionEntity(
                id = "q_2026-10-04_09",
                date = "2026-10-04",
                testId = "daily_2026-10-04",
                type = "synonym",
                question = "Select the closest synonym for 'UBIQUITOUS':",
                options = listOf("Rare", "Omnipresent", "Transient", "Esoteric"),
                correctAnswerIndex = 1,
                explanation = "'Ubiquitous' means present everywhere simultaneously, identical in meaning to 'omnipresent'.",
                topic = "vocabulary",
                difficulty = "easy",
                relatedWordOrRule = "ubiquitous"
            ),
            QuestionEntity(
                id = "q_2026-10-04_10",
                date = "2026-10-04",
                testId = "daily_2026-10-04",
                type = "sentence_correction",
                question = "Correct the dangling modifier: 'Having finished the fiscal audit, the report was submitted to the cabinet.'",
                options = listOf(
                    "Having finished the fiscal audit, the committee submitted the report to the cabinet.",
                    "Having finished the fiscal audit, submission of the report occurred quickly.",
                    "The report, having finished the fiscal audit, was submitted to the cabinet.",
                    "Having been finished by the fiscal audit, the report was submitted to the cabinet."
                ),
                correctAnswerIndex = 0,
                explanation = "The entity that 'finished the fiscal audit' was the committee, so 'the committee' must immediately follow the introductory participial phrase.",
                topic = "grammar",
                difficulty = "hard",
                relatedWordOrRule = "Dangling Modifiers and Participial Clauses"
            ),
            QuestionEntity(
                id = "q_2026-10-04_11",
                date = "2026-10-04",
                testId = "daily_2026-10-04",
                type = "meaning",
                question = "If an author refers to a 'SILVER BULLET', they mean:",
                options = listOf(
                    "A dangerous military escalation",
                    "A miraculous, instant cure-all for a complex problem",
                    "An expensive metallic reserve",
                    "A high-speed train network"
                ),
                correctAnswerIndex = 1,
                explanation = "A 'silver bullet' is a metaphor for a simple, magical panacea or instant remedy for a complicated problem.",
                topic = "expressions",
                difficulty = "easy",
                relatedWordOrRule = "Silver bullet"
            ),
            QuestionEntity(
                id = "q_2026-10-04_12",
                date = "2026-10-04",
                testId = "daily_2026-10-04",
                type = "mcq",
                question = "The term 'TENUOUS' implies a connection or relationship that is:",
                options = listOf("Robust and dependable", "Shaky, weak, and insubstantial", "Permanent and unyielding", "Violent and abrasive"),
                correctAnswerIndex = 1,
                explanation = "'Tenuous' describes something very weak, fragile, or doubtful.",
                topic = "vocabulary",
                difficulty = "medium",
                relatedWordOrRule = "tenuous"
            ),
            QuestionEntity(
                id = "q_2026-10-04_13",
                date = "2026-10-04",
                testId = "daily_2026-10-04",
                type = "fill_blank",
                question = "Among the foremost fiscal reform proposals _______ the rationalization of inverted duty structures.",
                options = listOf("is", "are", "were", "have been"),
                correctAnswerIndex = 0,
                explanation = "In the inverted sentence, the delayed subject is 'the rationalization' (singular), which governs the singular verb 'is'.",
                topic = "grammar",
                difficulty = "hard",
                relatedWordOrRule = "Subject-Verb Agreement in Inverted Sentences"
            ),
            QuestionEntity(
                id = "q_2026-10-04_14",
                date = "2026-10-04",
                testId = "daily_2026-10-04",
                type = "antonym",
                question = "What is the ANTONYM of 'TRANSIENT'?",
                options = listOf("Fleeting", "Enduring", "Ephemeral", "Nascent"),
                correctAnswerIndex = 1,
                explanation = "'Transient' means temporary or fleeting. Its direct opposite is 'enduring' or 'permanent'.",
                topic = "vocabulary",
                difficulty = "easy",
                relatedWordOrRule = "transient"
            ),
            QuestionEntity(
                id = "q_2026-10-04_15",
                date = "2026-10-04",
                testId = "daily_2026-10-04",
                type = "comprehension",
                question = "According to the editorial, which sector requires bolstering to absorb millions of young workers?",
                options = listOf(
                    "High-frequency algorithmic trading",
                    "Labor-intensive segments like textiles and food processing",
                    "Capital-intensive semiconductor foundries only",
                    "Foreign speculative real estate"
                ),
                correctAnswerIndex = 1,
                explanation = "The article explicitly advocates bolstering labor-intensive sectors like textiles and food processing to provide broad-based employment.",
                topic = "comprehension",
                difficulty = "medium",
                relatedWordOrRule = "bolster"
            )
        )
        testDao.insertQuestions(questions)

        // 7. Daily Test for 2026-10-04
        val dailyTest = TestEntity(
            id = "daily_2026-10-04",
            date = "2026-10-04",
            title = "Daily English Test: 04 October 2026",
            type = "daily",
            durationMinutes = 15,
            totalQuestions = 15,
            instructions = "15-question evaluation testing today's editorial vocabulary, grammar rules, expressions, and comprehension nuances."
        )
        testDao.insertTest(dailyTest)

        val testQuestionRefs = questions.mapIndexed { index, q ->
            TestQuestionCrossRef(testId = "daily_2026-10-04", questionId = q.id, orderIndex = index)
        }
        testDao.linkQuestionsToTest(testQuestionRefs)

        // 8. Daily Learning Progress record for today
        progressDao.insertOrUpdateDailyProgress(
            DailyProgressEntity(
                date = "2026-10-04",
                editorialRead = false,
                vocabLearnedCount = 0,
                totalVocabCount = 18,
                grammarStudiedCount = 0,
                totalGrammarCount = 4,
                expressionsLearnedCount = 0,
                totalExpressionsCount = 7,
                practiceAnsweredCount = 0,
                totalPracticeCount = 15,
                testTaken = false
            )
        )

        // 9. Also seed recent dates (2026-10-03 and 2026-10-02) for rich Calendar and Archive
        seedPreviousDay(database, "2026-10-03")
        seedPreviousDay(database, "2026-10-02")
    }

    private suspend fun seedPreviousDay(database: AppDatabase, date: String) {
        val editorialDao = database.editorialDao()
        val vocabDao = database.vocabularyDao()
        val progressDao = database.dailyProgressDao()
        val testDao = database.practiceTestDao()

        val title = if (date == "2026-10-03") "India's Clean Energy Leap: Navigating Grid Integration" else "The Governance of Artificial Intelligence in Global Trade"
        val ed = EditorialEntity(
            id = "ed_$date",
            date = date,
            title = title,
            source = "The Indian Express",
            readTimeMinutes = 5,
            contentMarkdown = """# $title

*Source: The Indian Express | Published: ${DateUtils.formatDate(date)}*

The rapid expansion of solar and wind generation marks a profound structural transformation in regional energy architecture. While capacity additions have set historic benchmarks, grid flexibility and long-duration storage remain the vital frontier.

Technological self-reliance and policy coherence will dictate whether this transformation can be achieved without imposing excessive tariff burdens on end consumers.
""".trimIndent(),
            isCompleted = true,
            isFavorite = (date == "2026-10-03")
        )
        editorialDao.insertEditorial(ed)

        // Sample words for previous day
        val word1 = if (date == "2026-10-03") "decouple" else "algorithmic"
        val v = VocabularyEntity(
            id = word1,
            word = word1.replaceFirstChar { it.uppercase() },
            partOfSpeech = if (date == "2026-10-03") "verb" else "adjective",
            meaning = if (date == "2026-10-03") "Separate or disassociate something from something else." else "Relating to or using a process or set of rules in calculations.",
            hindiMeaning = if (date == "2026-10-03") "अलग करना" else "एल्गोरिदम संबंधी",
            synonyms = listOf("separate", "isolate", "dissociate"),
            antonyms = listOf("couple", "connect", "link"),
            exampleSentence = "Policymakers sought to decouple economic expansion from carbon intensity.",
            difficulty = "medium",
            sourceArticle = title,
            learningStatus = WordLearningStatus.LEARNING.name,
            nextReviewDate = "2026-10-06"
        )
        vocabDao.insertWord(v)
        vocabDao.linkWordToEditorial(EditorialVocabularyCrossRef("ed_$date", v.id, date))

        progressDao.insertOrUpdateDailyProgress(
            DailyProgressEntity(
                date = date,
                editorialRead = true,
                vocabLearnedCount = 8,
                totalVocabCount = 12,
                grammarStudiedCount = 3,
                totalGrammarCount = 3,
                expressionsLearnedCount = 5,
                totalExpressionsCount = 5,
                practiceAnsweredCount = 10,
                totalPracticeCount = 10,
                testTaken = true,
                testScore = 9,
                testMaxScore = 10,
                isDayComplete = true
            )
        )
    }
}
