package com.example.data.pyq

import com.example.data.local.PyqEntity

object PyqData {
    fun getDefaultPyqs(): List<PyqEntity> {
        return listOf(
            // ================= PHYSICS PYQs =================
            PyqEntity(
                board = "BSEB",
                subject = "Physics",
                unit = "Unit I: Electrostatics",
                chapter = "Electric Charges & Fields",
                topic = "Gauss's Theorem",
                year = 2024,
                marks = 5,
                questionType = "LONG",
                difficulty = "Advanced",
                questionText = "State and prove Gauss's theorem in electrostatics. Using this theorem, derive an expression for electric field intensity due to an infinitely long straight uniformly charged wire.",
                optionsJson = "",
                answerText = "Gauss's Theorem states that the total electric flux through any closed surface is equal to 1/ε₀ times the total charge enclosed by the surface: ∮ E·dA = q_enclosed / ε₀.",
                stepByStepSolution = "Step 1: Consider a line charge of linear charge density λ.\nStep 2: Choose a cylindrical Gaussian surface of radius r and length L coaxial with the line charge.\nStep 3: Total electric flux ∮ E·dA = E × (2πrL) as flux through flat circular end faces is zero (E ⟂ dA).\nStep 4: By Gauss's law, E × 2πrL = q/ε₀ = (λL)/ε₀.\nStep 5: Therefore, E = λ / (2πε₀r)."
            ),
            PyqEntity(
                board = "BSEB",
                subject = "Physics",
                unit = "Unit I: Electrostatics",
                chapter = "Electrostatic Potential & Capacitance",
                topic = "Capacitance",
                year = 2023,
                marks = 1,
                questionType = "MCQ",
                difficulty = "Beginner",
                questionText = "The unit of electrical capacity (capacitance) is:",
                optionsJson = "[\"Volt\", \"Farad\", \"Newton\", \"Weber\"]",
                answerText = "Farad",
                stepByStepSolution = "Capacitance C = Q / V. 1 Coulomb / 1 Volt = 1 Farad (F)."
            ),
            PyqEntity(
                board = "BSEB",
                subject = "Physics",
                unit = "Unit II: Current Electricity",
                chapter = "Current Electricity",
                topic = "Kirchhoff's Laws",
                year = 2024,
                marks = 2,
                questionType = "SHORT",
                difficulty = "Medium",
                questionText = "State Kirchhoff's first and second rules. On what conservation laws are they based?",
                optionsJson = "",
                answerText = "1. Junction Rule (KCL): The algebraic sum of currents entering a junction is zero (Based on Conservation of Charge).\n2. Loop Rule (KVL): The algebraic sum of changes in potential around any closed loop is zero (Based on Conservation of Energy).",
                stepByStepSolution = "KCL: Σ I = 0 (Conservation of Electric Charge)\nKVL: Σ ΔV = 0 or Σ E = Σ IR (Conservation of Energy)"
            ),
            PyqEntity(
                board = "BSEB",
                subject = "Physics",
                unit = "Unit IV: EMI & AC",
                chapter = "Alternating Currents",
                topic = "Transformer",
                year = 2023,
                marks = 5,
                questionType = "LONG",
                difficulty = "Advanced",
                questionText = "Explain the principle and construction of a step-up and step-down transformer. Why is the core of a transformer laminated?",
                optionsJson = "",
                answerText = "Principle: Mutual Induction. Transformation ratio k = Vs/Vp = Ns/Np. The iron core is laminated with insulating varnish to minimize Eddy current losses (heat dissipation).",
                stepByStepSolution = "Step 1: Mutual induction principle - alternating current in primary induces alternating emf in secondary.\nStep 2: Mathematical relation: Es / Ep = Ns / Np = Ip / Is.\nStep 3: Energy losses include Copper loss, Iron/Eddy loss, Hysteresis loss, and Flux leakage.\nStep 4: Lamination cuts the continuous conductive path for eddy loops."
            ),
            PyqEntity(
                board = "BSEB",
                subject = "Physics",
                unit = "Unit VI: Optics",
                chapter = "Ray Optics",
                topic = "Refraction",
                year = 2022,
                marks = 1,
                questionType = "MCQ",
                difficulty = "Beginner",
                questionText = "The phenomenon responsible for the sparkling of diamonds is:",
                optionsJson = "[\"Total Internal Reflection\", \"Refraction\", \"Dispersion\", \"Interference\"]",
                answerText = "Total Internal Reflection",
                stepByStepSolution = "Diamonds have a high refractive index (μ ≈ 2.42) and a very small critical angle (θc ≈ 24.4°). Light entering the diamond undergoes multiple TIRs before emerging."
            ),

            // ================= CHEMISTRY PYQs =================
            PyqEntity(
                board = "BSEB",
                subject = "Chemistry",
                unit = "Physical Chemistry",
                chapter = "Solutions",
                topic = "Raoult's Law",
                year = 2024,
                marks = 2,
                questionType = "SHORT",
                difficulty = "Beginner",
                questionText = "Define Raoult's law for a solution containing a volatile solute and solvent.",
                optionsJson = "",
                answerText = "Raoult's law states that for a solution of volatile liquids, the partial vapour pressure of each component in the solution is directly proportional to its mole fraction in the solution: p₁ = p₁° x₁.",
                stepByStepSolution = "p_total = p₁ + p₂ = p₁°x₁ + p₂°x₂ = p₁° + (p₂° - p₁°)x₂."
            ),
            PyqEntity(
                board = "BSEB",
                subject = "Chemistry",
                unit = "Physical Chemistry",
                chapter = "Electrochemistry",
                topic = "Nernst Equation",
                year = 2023,
                marks = 5,
                questionType = "LONG",
                difficulty = "Advanced",
                questionText = "Write the Nernst equation for a galvanic cell. Calculate the EMF of the Daniel cell at 298 K when [Zn²⁺] = 0.1 M and [Cu²⁺] = 0.01 M. (E°_cell = 1.10 V)",
                optionsJson = "",
                answerText = "E_cell = 1.07 V",
                stepByStepSolution = "E_cell = E°_cell - (0.0591 / n) * log([Zn²⁺] / [Cu²⁺])\nHere n = 2.\nE_cell = 1.10 - (0.0591 / 2) * log(0.1 / 0.01)\nE_cell = 1.10 - 0.02955 * log(10) = 1.10 - 0.02955 ≈ 1.07 V."
            ),
            PyqEntity(
                board = "BSEB",
                subject = "Chemistry",
                unit = "Organic Chemistry",
                chapter = "Haloalkanes and Haloarenes",
                topic = "SN1 vs SN2",
                year = 2024,
                marks = 3,
                questionType = "SHORT",
                difficulty = "Medium",
                questionText = "Distinguish between SN1 and SN2 reaction mechanisms on the basis of kinetics, stereochemistry, and carbocation intermediate.",
                optionsJson = "",
                answerText = "SN1: Unimolecular (Rate ∝ [R-X]), 2 steps with Carbocation intermediate, Racemization occurs. Order: 3° > 2° > 1°.\nSN2: Bimolecular (Rate ∝ [R-X][Nu⁻]), 1 step concerted with Walden Inversion. Order: 1° > 2° > 3°.",
                stepByStepSolution = "1. Kinetics: SN1 is 1st order; SN2 is 2nd order.\n2. Intermediate: SN1 forms planar carbocation; SN2 proceeds through pentacoordinate transition state.\n3. Stereochemistry: SN1 gives partial racemization; SN2 gives 100% inversion."
            ),

            // ================= BIOLOGY PYQs =================
            PyqEntity(
                board = "BSEB",
                subject = "Biology",
                unit = "Unit I: Reproduction",
                chapter = "Sexual Reproduction in Flowering Plants",
                topic = "Double Fertilization",
                year = 2024,
                marks = 5,
                questionType = "LONG",
                difficulty = "Medium",
                questionText = "Describe double fertilization and triple fusion in angiosperms with a neat labelled diagram of the embryo sac.",
                optionsJson = "",
                answerText = "Double fertilization is the fusion of one male gamete with the egg cell (Syngamy → 2n Zygote) and the other male gamete with two polar nuclei (Triple Fusion → 3n Primary Endosperm Nucleus PEN).",
                stepByStepSolution = "1. Syngamy: Male gamete (n) + Egg (n) = Zygote (2n).\n2. Triple Fusion: Male gamete (n) + 2 Polar nuclei (2n) = PEN (3n).\n3. Endosperm nourishes the developing embryo."
            ),
            PyqEntity(
                board = "BSEB",
                subject = "Biology",
                unit = "Unit II: Genetics & Evolution",
                chapter = "Molecular Basis of Inheritance",
                topic = "DNA Structure",
                year = 2023,
                marks = 1,
                questionType = "MCQ",
                difficulty = "Beginner",
                questionText = "In a double-stranded DNA, if Adenine makes up 30% of bases, what will be the percentage of Guanine?",
                optionsJson = "[\"20%\", \"30%\", \"40%\", \"70%\"]",
                answerText = "20%",
                stepByStepSolution = "By Chargaff's Rule, A = T and G = C.\nIf A = 30%, then T = 30%. Total (A+T) = 60%.\nRemaining (G+C) = 100% - 60% = 40%.\nTherefore G = 40% / 2 = 20%."
            ),

            // ================= HINDI PYQs =================
            PyqEntity(
                board = "BSEB",
                subject = "Hindi",
                unit = "Literature: Digant Part-2 (Prose)",
                chapter = "उसने कहा था (चन्द्रधर शर्मा गुलेरी)",
                topic = "लहना सिंह",
                year = 2024,
                marks = 5,
                questionType = "LONG",
                difficulty = "Medium",
                questionText = "'उसने कहा था' कहानी का सारांश अपने शब्दों में लिखें तथा लहना सिंह के चरित्र की मुख्य विशेषताओं पर प्रकाश डालें।",
                optionsJson = "",
                answerText = "लहना सिंह कर्तव्यपरायण, निःस्वार्थ प्रेमी और वीर सिपाही है। उसने सूबेदारनी के दिए वचन ('मेरे पति और बेटे की रक्षा करना') को निभाने के लिए युद्ध के मैदान में अपने प्राणों का बलिदान दे दिया।",
                stepByStepSolution = "मुख्य बिंदु:\n1. अमृतसर के बाजार में 12 वर्षीय लहना सिंह और 8 वर्षीय बालिका (सूबेदारनी) की प्रथम भेंट।\n2. प्रथम विश्वयुद्ध के मोर्चे पर 77वीं सिख राइफल्स में जमादार के पद पर वीरता।\n3. सूबेदारनी के वचन की स्मृति और प्राणोत्सर्ग द्वारा वचन पालन।"
            ),
            PyqEntity(
                board = "BSEB",
                subject = "Hindi",
                unit = "Grammar: Vyakaran & Rachna",
                chapter = "संधि एवं समास",
                topic = "संधि विच्छेद",
                year = 2023,
                marks = 1,
                questionType = "MCQ",
                difficulty = "Beginner",
                questionText = "'पवन' शब्द का सही संधि-विच्छेद क्या है?",
                optionsJson = "[\"पो + अन\", \"पौ + अन\", \"प + वन\", \"पा + वन\"]",
                answerText = "पो + अन",
                stepByStepSolution = "अयादि स्वर संधि के नियमानुसार (ओ + अ = अव)। अतः पो + अन = पवन।"
            ),

            // ================= ENGLISH PYQs =================
            PyqEntity(
                board = "BSEB",
                subject = "English",
                unit = "Section B: Writing Skills",
                chapter = "Extended Writing",
                topic = "Letter Writing",
                year = 2024,
                marks = 5,
                questionType = "LONG",
                difficulty = "Medium",
                questionText = "Write a letter to the Editor of a national daily expressing concern over the growing menace of cyber-bullying and excessive smartphone addiction among students.",
                optionsJson = "",
                answerText = "Formal Letter Format: Sender's Address -> Date -> Receiver's Designation/Address -> Subject -> Salutation -> Body (3 paragraphs) -> Subscription (Yours sincerely).",
                stepByStepSolution = "Body Structure:\nPara 1: Through the columns of your esteemed newspaper, I wish to draw attention...\nPara 2: Unmonitored screen time leads to sleep deprivation, attention fragmentation, and mental stress.\nPara 3: Strict digital curfews, awareness workshops, and constructive physical outlets must be encouraged."
            )
        )
    }
}
