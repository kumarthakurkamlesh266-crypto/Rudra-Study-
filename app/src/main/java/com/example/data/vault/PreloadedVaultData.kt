package com.example.data.vault

import com.example.data.local.PatternEntity
import com.example.data.local.QuestionEntity
import com.example.data.local.VaultDocumentEntity
import org.json.JSONArray

object PreloadedVaultData {

    fun getDefaultVaultDocuments(): List<VaultDocumentEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            VaultDocumentEntity(
                id = 1,
                fileName = "BSEB_2024_Physics_Official_Question_Paper.pdf",
                title = "BSEB 2024 Physics Official Board Paper",
                fileUriOrPath = "vault_samples/bseb_2024_phy.pdf",
                fileSizeBytes = 2_450_000L, // 2.45 MB
                pageCount = 12,
                subject = "Physics",
                category = "PYQ_PAPER",
                board = "BSEB",
                uploadTimestamp = now - 86400000L * 3,
                extractedText = "SECTION A: 70 Multiple Choice Questions (Answer any 35). SECTION B: 20 Short Answer Questions (2 Marks each, Answer any 10). SECTION C: 6 Long Answer Questions (5 Marks each, Answer any 3). Topics: Gauss Theorem, Capacitance, LCR Circuits, Wave Optics, Photoelectric Effect, Logic Gates.",
                isAnalyzed = true,
                analyzedSummary = "Complete official BSEB 2024 Physics board paper containing 70 MCQs, 20 Short (2M), and 6 Long (5M) questions. Heavy focus on Electrostatics, Alternating Current, and Optics.",
                questionsCount = 14,
                patternsCount = 4,
                isBookmarked = true
            ),
            VaultDocumentEntity(
                id = 2,
                fileName = "CBSE_Chemistry_Organic_Reactions_Mechanisms.pdf",
                title = "Class 12 Organic Chemistry Name Reactions & Traps",
                fileUriOrPath = "vault_samples/organic_chem.pdf",
                fileSizeBytes = 3_820_000L, // 3.82 MB
                pageCount = 24,
                subject = "Chemistry",
                category = "QUESTION_BANK",
                board = "CBSE",
                uploadTimestamp = now - 86400000L * 2,
                extractedText = "Name Reactions: Aldol Condensation, Cannizzaro Reaction, Kolbe's Reaction, Reimer-Tiemann, Diazotization, Sandmeyer Reaction, Clemmensen and Wolff-Kishner reduction. Distinguishing chemical tests: Lucas test, Iodoform test, Tollen's reagent, Fehling's solution, Carbylamine test.",
                isAnalyzed = true,
                analyzedSummary = "High-yield Organic Chemistry question repository covering all 18 major name reactions, mechanisms, distinguishing functional group tests, and multi-step synthesis pathways.",
                questionsCount = 16,
                patternsCount = 5,
                isBookmarked = true
            ),
            VaultDocumentEntity(
                id = 3,
                fileName = "Biology_Genetics_Biotech_HighYield_Bank.pdf",
                title = "Genetics & Biotechnology Master Question Vault",
                fileUriOrPath = "vault_samples/biology_genetics.pdf",
                fileSizeBytes = 4_120_000L, // 4.12 MB
                pageCount = 30,
                subject = "Biology",
                category = "CHAPTER_NOTES",
                board = "BSEB",
                uploadTimestamp = now - 86400000L * 1,
                extractedText = "Mendelian inheritance, Dihybrid cross, Incomplete dominance, DNA Replication semi-conservative model (Meselson-Stahl), lac Operon model, Human Genome Project, Recombinant DNA technology, PCR steps, Restriction endonuclease enzymes, Gel electrophoresis, Bt Cotton.",
                isAnalyzed = true,
                analyzedSummary = "Comprehensive question & pattern repository for Unit 2 (Genetics & Evolution) and Unit 4 (Biotechnology) carrying 35+ board marks.",
                questionsCount = 12,
                patternsCount = 3,
                isBookmarked = false
            ),
            VaultDocumentEntity(
                id = 4,
                fileName = "Physics_Derivations_Formulae_Master_Sheet.pdf",
                title = "Class 12 Physics Top 25 Mandatory Derivations",
                fileUriOrPath = "vault_samples/physics_derivations.pdf",
                fileSizeBytes = 1_950_000L, // 1.95 MB
                pageCount = 16,
                subject = "Physics",
                category = "FORMULA_SHEET",
                board = "ALL",
                uploadTimestamp = now,
                extractedText = "Top 25 Derivations: 1. Electric field due to dipole (axial & equatorial). 2. Gauss Law: Infinite line charge, infinite plane sheet, spherical shell. 3. Parallel plate capacitor with dielectric slab. 4. Biot-Savart Law: Circular coil. 5. Ampere Circuital Law: Solenoid & Toroid. 6. Cyclotron & Moving Coil Galvanometer. 7. Self and Mutual Inductance. 8. LCR Series Circuit impedance & resonance. 9. Lens Maker's Formula. 10. Prism formula & dispersion. 11. Huygens Principle: Reflection & Refraction. 12. Young's Double Slit Experiment fringe width.",
                isAnalyzed = true,
                analyzedSummary = "Complete formula sheet and 25 guaranteed 5-mark derivation list with step-by-step calculus proofs and grading breakdown.",
                questionsCount = 10,
                patternsCount = 4,
                isBookmarked = true
            )
        )
    }

    fun getDefaultQuestions(): List<QuestionEntity> {
        val questions = mutableListOf<QuestionEntity>()

        // --- Physics Questions ---
        questions.add(
            QuestionEntity(
                sourceVaultDocId = 1,
                sourceDocName = "BSEB_2024_Physics_Official_Question_Paper.pdf",
                board = "BSEB",
                subject = "Physics",
                unitName = "Electrostatics",
                chapterName = "Electric Charges and Fields",
                topicName = "Gauss's Theorem",
                questionText = "State Gauss's Theorem in electrostatics. Using this theorem, derive an expression for the electric field intensity due to an infinitely long straight uniformly charged wire of linear charge density λ.",
                questionType = "LONG",
                optionsJson = "[]",
                correctAnswer = "E = λ / (2πε₀r) directed radially outwards for λ > 0.",
                stepByStepSolution = "1. Statement: Total electric flux Φ through a closed surface = q_enclosed / ε₀.\n2. Gaussian Surface: Cylindrical surface of radius r and length L coaxial with the charged wire.\n3. Flux Calculation: Φ = ∫ E·dA = E(2πrL) [circular flat ends contribute 0 flux because E ⊥ dA].\n4. Enclosed charge: q = λL.\n5. Equating: E(2πrL) = λL / ε₀ ⟹ E = λ / (2πε₀r).\n6. Vector form: **E** = (λ / 2πε₀r) r̂.",
                marks = 5,
                difficulty = "Advanced",
                frequencyScore = 5,
                yearOrSource = "BSEB 2024, 2022, 2019",
                isImportant = true
            )
        )

        questions.add(
            QuestionEntity(
                sourceVaultDocId = 1,
                sourceDocName = "BSEB_2024_Physics_Official_Question_Paper.pdf",
                board = "BSEB",
                subject = "Physics",
                unitName = "Electrostatics",
                chapterName = "Electrostatic Potential and Capacitance",
                topicName = "Parallel Plate Capacitor",
                questionText = "What is the equivalent capacitance when three identical capacitors of 6 µF each are connected in series?",
                questionType = "MCQ",
                optionsJson = JSONArray(listOf("(A) 18 µF", "(B) 2 µF", "(C) 6 µF", "(D) 0.5 µF")).toString(),
                correctAnswer = "(B) 2 µF",
                stepByStepSolution = "In series: 1/C_eq = 1/C₁ + 1/C₂ + 1/C₃ = 1/6 + 1/6 + 1/6 = 3/6 = 1/2.\nTherefore, C_eq = 2 µF.",
                marks = 1,
                difficulty = "Easy",
                frequencyScore = 4,
                yearOrSource = "BSEB 2024 Objective Set A",
                isImportant = false
            )
        )

        questions.add(
            QuestionEntity(
                sourceVaultDocId = 1,
                sourceDocName = "BSEB_2024_Physics_Official_Question_Paper.pdf",
                board = "BSEB",
                subject = "Physics",
                unitName = "Current Electricity",
                chapterName = "Current Electricity",
                topicName = "Kirchhoff's Laws",
                questionText = "State Kirchhoff's First Law (Junction Rule) and Second Law (Loop Rule). On which conservation principles are they based?",
                questionType = "SHORT",
                optionsJson = "[]",
                correctAnswer = "Junction Rule: ΣI = 0 (Conservation of Charge). Loop Rule: ΣΔV = 0 (Conservation of Energy).",
                stepByStepSolution = "1. First Law (KCL): Algebraic sum of currents meeting at any junction in a circuit is zero (ΣI = 0). It is based on Conservation of Electric Charge.\n2. Second Law (KVL): Algebraic sum of changes in potential around any closed loop is zero (ΣIR + Σε = 0). It is based on Conservation of Energy.",
                marks = 2,
                difficulty = "Medium",
                frequencyScore = 5,
                yearOrSource = "BSEB 2023, CBSE 2024",
                isImportant = true
            )
        )

        questions.add(
            QuestionEntity(
                sourceVaultDocId = 4,
                sourceDocName = "Physics_Derivations_Formulae_Master_Sheet.pdf",
                board = "CBSE",
                subject = "Physics",
                unitName = "Electromagnetic Induction & AC",
                chapterName = "Alternating Current",
                topicName = "LCR Series Resonance",
                questionText = "Derive the condition for electrical resonance in an LCR series AC circuit. Hence obtain the expression for resonant frequency and Q-factor (Quality factor).",
                questionType = "LONG",
                optionsJson = "[]",
                correctAnswer = "Resonant frequency f₀ = 1 / (2π√(LC)), Q = (1/R)√(L/C).",
                stepByStepSolution = "1. Impedance: Z = √(R² + (X_L - X_C)²), where X_L = ωL and X_C = 1/(ωC).\n2. Condition for Resonance: Current is maximum when Z is minimum ⟹ X_L = X_C ⟹ ω₀L = 1/(ω₀C).\n3. ω₀² = 1/(LC) ⟹ ω₀ = 1/√(LC) ⟹ f₀ = 1 / (2π√(LC)).\n4. At resonance, Z = R (purely resistive, phase angle φ = 0, power factor cos φ = 1).\n5. Q-Factor: Q = (ω₀L) / R = (1/R)√(L/C).",
                marks = 5,
                difficulty = "Advanced",
                frequencyScore = 5,
                yearOrSource = "CBSE 2024, BSEB 2023",
                isImportant = true
            )
        )

        questions.add(
            QuestionEntity(
                sourceVaultDocId = 4,
                sourceDocName = "Physics_Derivations_Formulae_Master_Sheet.pdf",
                board = "ALL",
                subject = "Physics",
                unitName = "Optics",
                chapterName = "Ray Optics and Optical Instruments",
                topicName = "Lens Maker's Formula",
                questionText = "Derive Lens Maker's Formula: 1/f = (μ - 1) [ 1/R₁ - 1/R₂ ] for a thin convex lens.",
                questionType = "LONG",
                optionsJson = "[]",
                correctAnswer = "1/f = (μ₂/μ₁ - 1)(1/R₁ - 1/R₂).",
                stepByStepSolution = "1. For refraction at 1st spherical surface: (μ₂/v₁) - (μ₁/u) = (μ₂ - μ₁)/R₁.\n2. For refraction at 2nd spherical surface: (μ₁/v) - (μ₂/v₁) = (μ₁ - μ₂)/R₂ = -(μ₂ - μ₁)/R₂.\n3. Adding equations: μ₁(1/v - 1/u) = (μ₂ - μ₁)(1/R₁ - 1/R₂).\n4. When u = ∞, v = f ⟹ 1/f = (μ₂/μ₁ - 1)(1/R₁ - 1/R₂).",
                marks = 5,
                difficulty = "Advanced",
                frequencyScore = 5,
                yearOrSource = "BSEB 2024 & CBSE 2023",
                isImportant = true
            )
        )

        questions.add(
            QuestionEntity(
                sourceVaultDocId = 1,
                sourceDocName = "BSEB_2024_Physics_Official_Question_Paper.pdf",
                board = "BSEB",
                subject = "Physics",
                unitName = "Modern Physics",
                chapterName = "Dual Nature of Radiation and Matter",
                topicName = "Photoelectric Effect",
                questionText = "The work function of a metal is 2.14 eV. Find the threshold frequency for this metal (h = 6.63 × 10⁻³⁴ J·s, 1 eV = 1.6 × 10⁻¹⁹ J).",
                questionType = "NUMERICAL",
                optionsJson = "[]",
                correctAnswer = "ν₀ = 5.16 × 10¹⁴ Hz",
                stepByStepSolution = "1. Formula: Φ₀ = h ν₀ ⟹ ν₀ = Φ₀ / h.\n2. Convert Φ₀ to Joules: Φ₀ = 2.14 × 1.6 × 10⁻¹⁹ J = 3.424 × 10⁻¹⁹ J.\n3. Calculate: ν₀ = (3.424 × 10⁻¹⁹) / (6.63 × 10⁻³⁴) = 5.164 × 10¹⁴ Hz.",
                marks = 2,
                difficulty = "Medium",
                frequencyScore = 4,
                yearOrSource = "BSEB 2024 Numerical",
                isImportant = true
            )
        )

        // --- Chemistry Questions ---
        questions.add(
            QuestionEntity(
                sourceVaultDocId = 2,
                sourceDocName = "CBSE_Chemistry_Organic_Reactions_Mechanisms.pdf",
                board = "CBSE",
                subject = "Chemistry",
                unitName = "Organic Chemistry",
                chapterName = "Aldehydes, Ketones and Carboxylic Acids",
                topicName = "Aldol Condensation",
                questionText = "Explain Aldol Condensation with a balanced chemical equation and mechanism. Why does formaldehyde (HCHO) NOT give aldol condensation?",
                questionType = "LONG",
                optionsJson = "[]",
                correctAnswer = "Requires presence of at least one α-hydrogen atom. HCHO has no α-hydrogen, hence gives Cannizzaro reaction instead.",
                stepByStepSolution = "1. Principle: Aldehydes and ketones having at least one α-hydrogen undergo self-condensation in presence of dilute alkali (NaOH/Ba(OH)₂) to form β-hydroxyaldehydes (aldol) or β-hydroxyketones (ketol).\n2. Reaction: 2 CH₃CHO --(dil. NaOH)--> CH₃-CH(OH)-CH₂-CHO --(Heat, -H₂O)--> CH₃-CH=CH-CHO (But-2-enal / Crotonaldehyde).\n3. Formaldehyde (H-CHO) has no α-carbon and thus no α-hydrogen, hence it cannot form an enolate ion to undergo aldol condensation; it undergoes Cannizzaro reaction instead.",
                marks = 5,
                difficulty = "Advanced",
                frequencyScore = 5,
                yearOrSource = "CBSE 2024, BSEB 2023",
                isImportant = true
            )
        )

        questions.add(
            QuestionEntity(
                sourceVaultDocId = 2,
                sourceDocName = "CBSE_Chemistry_Organic_Reactions_Mechanisms.pdf",
                board = "ALL",
                subject = "Chemistry",
                unitName = "Physical Chemistry",
                chapterName = "Solutions",
                topicName = "Raoult's Law & Colligative Properties",
                questionText = "State Raoult's Law for a solution containing non-volatile solute. Write the relation between relative lowering of vapour pressure and molar mass of solute.",
                questionType = "SHORT",
                optionsJson = "[]",
                correctAnswer = "(P° - P) / P° = x_B = (w_B × M_A) / (M_B × w_A).",
                stepByStepSolution = "1. Statement: Relative lowering of vapour pressure of a dilute solution containing a non-volatile solute is equal to the mole fraction of the solute in the solution.\n2. Formula: (P_A° - P_A) / P_A° = x_B = n_B / (n_A + n_B) ≈ n_B / n_A (for dilute solution).\n3. Expanding: (P_A° - P_A) / P_A° = (w_B / M_B) / (w_A / M_A) = (w_B × M_A) / (M_B × w_A).\n4. M_B = (w_B × M_A × P_A°) / (w_A × (P_A° - P_A)).",
                marks = 3,
                difficulty = "Medium",
                frequencyScore = 5,
                yearOrSource = "BSEB 2024, CBSE 2023",
                isImportant = true
            )
        )

        questions.add(
            QuestionEntity(
                sourceVaultDocId = 2,
                sourceDocName = "CBSE_Chemistry_Organic_Reactions_Mechanisms.pdf",
                board = "BSEB",
                subject = "Chemistry",
                unitName = "Physical Chemistry",
                chapterName = "Electrochemistry",
                topicName = "Kohlrausch's Law",
                questionText = "Which law states that limiting molar conductivity of an electrolyte can be represented as the sum of the individual contributions of the anion and cation?",
                questionType = "MCQ",
                optionsJson = JSONArray(listOf("(A) Faraday's Law", "(B) Kohlrausch's Law", "(C) Nernst Equation", "(D) Henry's Law")).toString(),
                correctAnswer = "(B) Kohlrausch's Law",
                stepByStepSolution = "Kohlrausch's law of independent migration of ions states that limiting molar conductivity of an electrolyte is the sum of limiting molar conductivities of its individual ions: Λ°_m = ν₊ λ°₊ + ν₋ λ°₋.",
                marks = 1,
                difficulty = "Easy",
                frequencyScore = 4,
                yearOrSource = "BSEB 2024 Objective Set B",
                isImportant = false
            )
        )

        // --- Biology Questions ---
        questions.add(
            QuestionEntity(
                sourceVaultDocId = 3,
                sourceDocName = "Biology_Genetics_Biotech_HighYield_Bank.pdf",
                board = "BSEB",
                subject = "Biology",
                unitName = "Genetics and Evolution",
                chapterName = "Molecular Basis of Inheritance",
                topicName = "DNA Replication & Meselson-Stahl",
                questionText = "Describe the experiment conducted by Meselson and Stahl that proved that DNA replication is semi-conservative in nature.",
                questionType = "LONG",
                optionsJson = "[]",
                correctAnswer = "Used E. coli grown in ¹⁵NH₄Cl (heavy nitrogen) and transferred to ¹⁴NH₄Cl (light nitrogen) medium, observed CsCl density gradient centrifugation.",
                stepByStepSolution = "1. Setup: Cultured E. coli in ¹⁵N isotope medium for many generations so all DNA was ¹⁵N-¹⁵N (heavy).\n2. Generation 1 (20 min in ¹⁴N medium): Centrifugation in CsCl showed a single hybrid intermediate density band (¹⁵N-¹⁴N).\n3. Generation 2 (40 min in ¹⁴N medium): Centrifugation showed equal amounts of two bands: Light DNA (¹⁴N-¹⁴N) and Hybrid DNA (¹⁵N-¹⁴N).\n4. Conclusion: Each daughter DNA molecule retains one parental strand and synthesizes one new complementary strand, proving semi-conservative replication.",
                marks = 5,
                difficulty = "Advanced",
                frequencyScore = 5,
                yearOrSource = "BSEB 2024, CBSE 2024",
                isImportant = true
            )
        )

        questions.add(
            QuestionEntity(
                sourceVaultDocId = 3,
                sourceDocName = "Biology_Genetics_Biotech_HighYield_Bank.pdf",
                board = "ALL",
                subject = "Biology",
                unitName = "Biotechnology",
                chapterName = "Biotechnology: Principles and Processes",
                topicName = "Polymerase Chain Reaction (PCR)",
                questionText = "Explain the three main steps of Polymerase Chain Reaction (PCR). Name the heat-stable DNA polymerase used in this technique and its source organism.",
                questionType = "SHORT",
                optionsJson = "[]",
                correctAnswer = "Steps: 1. Denaturation (94°C), 2. Primer Annealing (54°C), 3. Extension (72°C). Enzyme: Taq Polymerase from bacterium Thermus aquaticus.",
                stepByStepSolution = "1. Denaturation: Double-stranded target DNA is heated to 94°C-96°C to separate into single strands.\n2. Annealing: Temperature lowered to ~50°C-56°C allowing oligonucleotide primers to bind to complementary ends.\n3. Extension: Temperature raised to 72°C where Taq Polymerase synthesizes new strand using dNTPs.\n4. Repeated for 30 cycles to amplify DNA 1 billion-fold.",
                marks = 3,
                difficulty = "Medium",
                frequencyScore = 5,
                yearOrSource = "BSEB 2023, CBSE 2023",
                isImportant = true
            )
        )

        // --- Mathematics Questions ---
        questions.add(
            QuestionEntity(
                sourceVaultDocId = 1,
                sourceDocName = "BSEB_2024_Physics_Official_Question_Paper.pdf",
                board = "BSEB",
                subject = "Mathematics",
                unitName = "Calculus",
                chapterName = "Integrals",
                topicName = "Definite Integrals Properties",
                questionText = "Evaluate the definite integral: ∫₀^(π/2) [ √(sin x) / (√(sin x) + √(cos x)) ] dx.",
                questionType = "SHORT",
                optionsJson = "[]",
                correctAnswer = "π / 4",
                stepByStepSolution = "1. Let I = ∫₀^(π/2) [ √sin x / (√sin x + √cos x) ] dx --- (1)\n2. Property: ∫₀ᵃ f(x) dx = ∫₀ᵃ f(a - x) dx.\n3. I = ∫₀^(π/2) [ √sin(π/2 - x) / (√sin(π/2 - x) + √cos(π/2 - x)) ] dx\n   I = ∫₀^(π/2) [ √cos x / (√cos x + √sin x) ] dx --- (2)\n4. Adding (1) and (2): 2I = ∫₀^(π/2) [ (√sin x + √cos x) / (√sin x + √cos x) ] dx = ∫₀^(π/2) 1 dx = [x]₀^(π/2) = π/2.\n5. Therefore, I = π / 4.",
                marks = 5,
                difficulty = "Advanced",
                frequencyScore = 5,
                yearOrSource = "BSEB 2024, CBSE 2023",
                isImportant = true
            )
        )

        return questions
    }

    fun getDefaultPatterns(): List<PatternEntity> {
        return listOf(
            PatternEntity(
                id = 1,
                sourceVaultDocId = 1,
                subject = "Physics",
                chapterName = "Electric Charges and Fields",
                topicName = "Gauss Law Applications",
                patternType = "HIGH_WEIGHTAGE_DERIVATION",
                frequency = 5,
                averageMarks = 5,
                title = "Gauss Law 5-Mark Mandatory Proof",
                description = "Gauss Law proof for either Infinite wire (λ/2πε₀r), Infinite plane sheet (σ/2ε₀), or Spherical Shell appears in Section C of BSEB every alternate year.",
                weightagePercentage = 22,
                examTip = "Always draw a neat 3D cylinder/pillbox Gaussian surface with area vector dA arrows and state flux through flat vs curved faces explicitly."
            ),
            PatternEntity(
                id = 2,
                sourceVaultDocId = 4,
                subject = "Physics",
                chapterName = "Alternating Current",
                topicName = "LCR Series Circuit",
                patternType = "REPEATED_CONCEPT",
                frequency = 4,
                averageMarks = 5,
                title = "LCR Resonance & Power Factor Trap",
                description = "Examiner tests the condition X_L = X_C, phasor diagram, impedance triangle, and asks why power factor is 1 at resonance.",
                weightagePercentage = 18,
                examTip = "Remember at resonance: Current is MAXIMUM, Impedance is MINIMUM (= R), and current is in phase with voltage (cos φ = 1)."
            ),
            PatternEntity(
                id = 3,
                sourceVaultDocId = 2,
                subject = "Chemistry",
                chapterName = "Aldehydes, Ketones and Carboxylic Acids",
                topicName = "Name Reactions",
                patternType = "HIGH_WEIGHTAGE_DERIVATION",
                frequency = 6,
                averageMarks = 5,
                title = "Aldol vs Cannizzaro Alpha-Hydrogen Discriminator",
                description = "Board questions consistently test whether the candidate knows that α-hydrogen presence leads to Aldol while absence (Benzaldehyde/Formaldehyde) leads to Cannizzaro.",
                weightagePercentage = 25,
                examTip = "Write products with heating (-H₂O) for Aldol (α,β-unsaturated aldehyde) and disproportionation products (alcohol + acid salt) for Cannizzaro."
            ),
            PatternEntity(
                id = 4,
                sourceVaultDocId = 2,
                subject = "Chemistry",
                chapterName = "Solutions",
                topicName = "Colligative Properties & Van't Hoff Factor",
                patternType = "FAVORITE_NUMERICAL_TYPE",
                frequency = 4,
                averageMarks = 3,
                title = "Elevation in Boiling Point & Freezing Depression Numericals",
                description = "Direct 3-mark numerical solving for Molar Mass M₂ using ΔT_b = i K_b m or ΔT_f = i K_f m with dissociation factor i.",
                weightagePercentage = 15,
                examTip = "Do not forget the Van't Hoff factor i for ionic solutes (e.g. i=2 for NaCl, i=3 for CaCl₂). Missing i is the #1 mistake."
            ),
            PatternEntity(
                id = 5,
                sourceVaultDocId = 3,
                subject = "Biology",
                chapterName = "Molecular Basis of Inheritance",
                topicName = "Genetic Code & Meselson-Stahl",
                patternType = "COMMON_BOARD_TRAP",
                frequency = 4,
                averageMarks = 5,
                title = "Heavy Nitrogen ¹⁵N is NOT Radioactive Trap",
                description = "Students mistakenly write ¹⁵N is radioactive. Examiners penalize this immediately. ¹⁵N is a heavy stable isotope separated by density gradient centrifugation.",
                weightagePercentage = 15,
                examTip = "Clearly specify: ¹⁵N is a heavy isotope, not radioactive. Centrifugation medium is Caesium Chloride (CsCl)."
            ),
            PatternEntity(
                id = 6,
                sourceVaultDocId = 1,
                subject = "Mathematics",
                chapterName = "Integrals",
                topicName = "Definite Integral King Property",
                patternType = "HIGH_WEIGHTAGE_DERIVATION",
                frequency = 5,
                averageMarks = 5,
                title = "The ∫₀ᵃ f(x)dx = ∫₀ᵃ f(a-x)dx King Property",
                description = "Appears in every board paper (both BSEB 5-mark and CBSE 4-mark). Always evaluate by creating (1) and (2) and adding to get 2I = ∫ 1 dx.",
                weightagePercentage = 20,
                examTip = "Write the property name clearly in the right-hand margin with a bracket to secure full method marks."
            )
        )
    }
}
