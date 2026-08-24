package com.example.data.syllabus

import com.example.data.local.SubjectEntity
import com.example.data.local.SyllabusChapterEntity
import com.example.data.local.TopicProgressEntity
import com.example.data.local.UnitEntity

data class SyllabusTopicModel(
    val id: String,
    val name: String,
    val difficulty: String = "Medium"
)

data class SyllabusChapterModel(
    val name: String,
    val topics: List<SyllabusTopicModel>
)

data class SyllabusUnitModel(
    val unitNumber: Int,
    val unitName: String,
    val chapters: List<SyllabusChapterModel>
)

data class SyllabusSubjectModel(
    val subjectName: String,
    val units: List<SyllabusUnitModel>
)

object SyllabusData {

    fun getDefaultSyllabus(board: String = "BSEB"): List<TopicProgressEntity> {
        val list = mutableListOf<TopicProgressEntity>()
        var counter = 1

        fun add(
            subject: String,
            unit: String,
            chapter: String,
            topic: String,
            diff: String = "Medium"
        ) {
            val safeSub = subject.take(3).uppercase()
            val safeUnit = unit.filter { it.isLetterOrDigit() }.take(6)
            val safeChap = chapter.filter { it.isLetterOrDigit() }.take(6)
            val safeTopic = topic.filter { it.isLetterOrDigit() }.take(8)
            val id = "${board}_${safeSub}_${safeUnit}_${safeChap}_${safeTopic}_$counter"

            val subjectId = "${board}_${subject.filter { it.isLetterOrDigit() }}"
            val unitId = "${subjectId}_${unit.filter { it.isLetterOrDigit() }}"
            val chapterId = "${unitId}_${chapter.filter { it.isLetterOrDigit() }}"

            list.add(
                TopicProgressEntity(
                    topicId = id,
                    chapterId = chapterId,
                    unitId = unitId,
                    subjectId = subjectId,
                    board = board,
                    subject = subject,
                    unitName = unit,
                    chapterName = chapter,
                    topicName = topic,
                    difficulty = diff,
                    status = "NOT_STARTED",
                    completionPercent = 0,
                    revisionCount = 0,
                    lastRevisedTimestamp = 0L,
                    isWeakTopic = false,
                    orderIndex = counter
                )
            )
            counter++
        }

        // =========================================================================
        // 1. PHYSICS (Class XII) - 10 Units
        // =========================================================================

        // Unit 1: Electrostatics
        // Chapter: Electric Charges and Fields
        add("Physics", "Unit 1: Electrostatics", "Electric Charges and Fields", "Electric charges and conservation of charge", "Beginner")
        add("Physics", "Unit 1: Electrostatics", "Electric Charges and Fields", "Coulomb's Law and forces between multiple charges", "Beginner")
        add("Physics", "Unit 1: Electrostatics", "Electric Charges and Fields", "Superposition principle and continuous charge distribution", "Medium")
        add("Physics", "Unit 1: Electrostatics", "Electric Charges and Fields", "Electric field and electric field lines", "Beginner")
        add("Physics", "Unit 1: Electrostatics", "Electric Charges and Fields", "Electric dipole and torque on a dipole in a uniform electric field", "Medium")
        add("Physics", "Unit 1: Electrostatics", "Electric Charges and Fields", "Electric flux and Gauss's Theorem", "Medium")
        add("Physics", "Unit 1: Electrostatics", "Electric Charges and Fields", "Gauss Law Application: Infinitely long straight wire", "Advanced")
        add("Physics", "Unit 1: Electrostatics", "Electric Charges and Fields", "Gauss Law Application: Uniformly charged infinite plane sheet", "Advanced")
        add("Physics", "Unit 1: Electrostatics", "Electric Charges and Fields", "Gauss Law Application: Uniformly charged thin spherical shell", "Advanced")

        // Chapter: Electrostatic Potential and Capacitance
        add("Physics", "Unit 1: Electrostatics", "Electrostatic Potential and Capacitance", "Electric potential and potential difference", "Beginner")
        add("Physics", "Unit 1: Electrostatics", "Electrostatic Potential and Capacitance", "Electric potential due to a point charge and electric dipole", "Medium")
        add("Physics", "Unit 1: Electrostatics", "Electrostatic Potential and Capacitance", "Equipotential surfaces and electrostatic potential energy", "Medium")
        add("Physics", "Unit 1: Electrostatics", "Electrostatic Potential and Capacitance", "Conductors, insulators, free and bound charges", "Beginner")
        add("Physics", "Unit 1: Electrostatics", "Electrostatic Potential and Capacitance", "Dielectrics and polarization", "Medium")
        add("Physics", "Unit 1: Electrostatics", "Electrostatic Potential and Capacitance", "Capacitors and capacitance (Parallel plate capacitor)", "Medium")
        add("Physics", "Unit 1: Electrostatics", "Electrostatic Potential and Capacitance", "Series and parallel combinations of capacitors", "Medium")
        add("Physics", "Unit 1: Electrostatics", "Electrostatic Potential and Capacitance", "Energy stored in a capacitor", "Advanced")
        add("Physics", "Unit 1: Electrostatics", "Electrostatic Potential and Capacitance", "Van de Graaff generator (Principle and working)", "Advanced")

        // Unit 2: Current Electricity
        // Chapter: Current Electricity
        add("Physics", "Unit 2: Current Electricity", "Current Electricity", "Electric current, drift velocity and mobility", "Beginner")
        add("Physics", "Unit 2: Current Electricity", "Current Electricity", "Ohm's Law, electrical resistance and V-I characteristics", "Beginner")
        add("Physics", "Unit 2: Current Electricity", "Current Electricity", "Electrical resistivity, conductivity and color code for resistors", "Beginner")
        add("Physics", "Unit 2: Current Electricity", "Current Electricity", "Series and parallel combinations of resistors", "Beginner")
        add("Physics", "Unit 2: Current Electricity", "Current Electricity", "Temperature dependence of resistance", "Medium")
        add("Physics", "Unit 2: Current Electricity", "Current Electricity", "Internal resistance of a cell, EMF and terminal potential difference", "Medium")
        add("Physics", "Unit 2: Current Electricity", "Current Electricity", "Combinations of cells in series and parallel", "Medium")
        add("Physics", "Unit 2: Current Electricity", "Current Electricity", "Kirchhoff's Laws and circuit applications", "Advanced")
        add("Physics", "Unit 2: Current Electricity", "Current Electricity", "Wheatstone bridge principle and Metre bridge", "Advanced")
        add("Physics", "Unit 2: Current Electricity", "Current Electricity", "Potentiometer: Principle and comparison of EMF of two cells", "Advanced")
        add("Physics", "Unit 2: Current Electricity", "Current Electricity", "Potentiometer: Determination of internal resistance of a cell", "Advanced")

        // Unit 3: Magnetic Effects of Current and Magnetism
        // Chapter: Moving Charges and Magnetism
        add("Physics", "Unit 3: Magnetic Effects of Current and Magnetism", "Moving Charges and Magnetism", "Biot-Savart Law and application to circular current loop", "Beginner")
        add("Physics", "Unit 3: Magnetic Effects of Current and Magnetism", "Moving Charges and Magnetism", "Ampere's Circuital Law and its applications (Solenoid & Toroid)", "Medium")
        add("Physics", "Unit 3: Magnetic Effects of Current and Magnetism", "Moving Charges and Magnetism", "Force on a moving charge in magnetic/electric fields (Lorentz force)", "Medium")
        add("Physics", "Unit 3: Magnetic Effects of Current and Magnetism", "Moving Charges and Magnetism", "Cyclotron: Principle, construction and working", "Advanced")
        add("Physics", "Unit 3: Magnetic Effects of Current and Magnetism", "Moving Charges and Magnetism", "Force on a current-carrying conductor in a magnetic field", "Medium")
        add("Physics", "Unit 3: Magnetic Effects of Current and Magnetism", "Moving Charges and Magnetism", "Force between two parallel current-carrying conductors & definition of Ampere", "Advanced")
        add("Physics", "Unit 3: Magnetic Effects of Current and Magnetism", "Moving Charges and Magnetism", "Torque experienced by a current loop in a uniform magnetic field", "Medium")
        add("Physics", "Unit 3: Magnetic Effects of Current and Magnetism", "Moving Charges and Magnetism", "Moving Coil Galvanometer: Working and sensitivity", "Advanced")
        add("Physics", "Unit 3: Magnetic Effects of Current and Magnetism", "Moving Charges and Magnetism", "Conversion of Galvanometer to Ammeter and Voltmeter", "Advanced")

        // Chapter: Magnetism and Matter
        add("Physics", "Unit 3: Magnetic Effects of Current and Magnetism", "Magnetism and Matter", "Current loop as a magnetic dipole and magnetic dipole moment", "Beginner")
        add("Physics", "Unit 3: Magnetic Effects of Current and Magnetism", "Magnetism and Matter", "Magnetic dipole moment of a revolving electron (Bohr magneton)", "Medium")
        add("Physics", "Unit 3: Magnetic Effects of Current and Magnetism", "Magnetism and Matter", "Earth's magnetic field and magnetic elements (Declination, Dip, Horizontal component)", "Medium")
        add("Physics", "Unit 3: Magnetic Effects of Current and Magnetism", "Magnetism and Matter", "Magnetic materials: Diamagnetic, Paramagnetic, and Ferromagnetic substances", "Beginner")
        add("Physics", "Unit 3: Magnetic Effects of Current and Magnetism", "Magnetism and Matter", "Electromagnets, permanent magnets and hysteresis curve", "Medium")

        // Unit 4: Electromagnetic Induction and Alternating Current
        // Chapter: Electromagnetic Induction
        add("Physics", "Unit 4: Electromagnetic Induction and AC", "Electromagnetic Induction", "Faraday's laws of electromagnetic induction and induced EMF", "Beginner")
        add("Physics", "Unit 4: Electromagnetic Induction and AC", "Electromagnetic Induction", "Lenz's Law and conservation of energy", "Beginner")
        add("Physics", "Unit 4: Electromagnetic Induction and AC", "Electromagnetic Induction", "Eddy currents and practical applications", "Medium")
        add("Physics", "Unit 4: Electromagnetic Induction and AC", "Electromagnetic Induction", "Self-induction, mutual induction and coefficient of coupling", "Medium")
        add("Physics", "Unit 4: Electromagnetic Induction and AC", "Electromagnetic Induction", "Displacement current and Maxwell's equations", "Advanced")

        // Chapter: Alternating Currents
        add("Physics", "Unit 4: Electromagnetic Induction and AC", "Alternating Currents", "Alternating currents: Peak and RMS value of AC voltage and current", "Beginner")
        add("Physics", "Unit 4: Electromagnetic Induction and AC", "Alternating Currents", "Reactance and impedance (Inductive & Capacitive)", "Medium")
        add("Physics", "Unit 4: Electromagnetic Induction and AC", "Alternating Currents", "LC oscillations (Qualitative treatment)", "Medium")
        add("Physics", "Unit 4: Electromagnetic Induction and AC", "Alternating Currents", "LCR series circuit analysis and phasor diagrams", "Advanced")
        add("Physics", "Unit 4: Electromagnetic Induction and AC", "Alternating Currents", "Resonance in LCR circuits and Quality factor (Q-factor)", "Advanced")
        add("Physics", "Unit 4: Electromagnetic Induction and AC", "Alternating Currents", "Power in AC circuits, power factor and wattless current", "Medium")
        add("Physics", "Unit 4: Electromagnetic Induction and AC", "Alternating Currents", "AC Generator: Principle, construction and working", "Advanced")
        add("Physics", "Unit 4: Electromagnetic Induction and AC", "Alternating Currents", "Transformer: Principle, energy losses and efficiency", "Advanced")

        // Unit 5: Electromagnetic Waves
        // Chapter: Electromagnetic Waves
        add("Physics", "Unit 5: Electromagnetic Waves", "Electromagnetic Waves", "Characteristics and transverse nature of EM waves", "Beginner")
        add("Physics", "Unit 5: Electromagnetic Waves", "Electromagnetic Waves", "Electromagnetic spectrum: Radio waves, Microwaves, Infrared, Visible light", "Beginner")
        add("Physics", "Unit 5: Electromagnetic Waves", "Electromagnetic Waves", "Electromagnetic spectrum: Ultraviolet, X-rays, Gamma rays and basic applications", "Beginner")

        // Unit 6: Optics
        // Chapter: Ray Optics
        add("Physics", "Unit 6: Optics", "Ray Optics", "Reflection of light, spherical mirrors and mirror formula", "Beginner")
        add("Physics", "Unit 6: Optics", "Ray Optics", "Refraction of light and Total Internal Reflection (TIR)", "Beginner")
        add("Physics", "Unit 6: Optics", "Ray Optics", "Optical fibers and practical applications of TIR", "Medium")
        add("Physics", "Unit 6: Optics", "Ray Optics", "Refraction at spherical surfaces and thin lens formula", "Medium")
        add("Physics", "Unit 6: Optics", "Ray Optics", "Lens Maker's Formula and magnification", "Advanced")
        add("Physics", "Unit 6: Optics", "Ray Optics", "Power of a lens and combination of thin lenses in contact", "Medium")
        add("Physics", "Unit 6: Optics", "Ray Optics", "Refraction and dispersion of light through a prism", "Medium")
        add("Physics", "Unit 6: Optics", "Ray Optics", "Scattering of light (Blue color of sky & reddish appearance of sun)", "Beginner")

        // Chapter: Optical Instruments
        add("Physics", "Unit 6: Optics", "Optical Instruments", "Human eye: Image formation, accommodation, defects and correction (Myopia, Hypermetropia)", "Medium")
        add("Physics", "Unit 6: Optics", "Optical Instruments", "Simple Microscope and Compound Microscope: Ray diagram and magnifying power", "Advanced")
        add("Physics", "Unit 6: Optics", "Optical Instruments", "Astronomical Telescope (Refracting & Reflecting Cassegrain): Ray diagram and magnification", "Advanced")

        // Chapter: Wave Optics
        add("Physics", "Unit 6: Optics", "Wave Optics", "Wavefront and Huygens' principle", "Beginner")
        add("Physics", "Unit 6: Optics", "Wave Optics", "Proof of laws of reflection and refraction of plane waves using Huygens' principle", "Advanced")
        add("Physics", "Unit 6: Optics", "Wave Optics", "Interference of light waves and Young's Double-Slit Experiment (YDSE)", "Advanced")
        add("Physics", "Unit 6: Optics", "Wave Optics", "Fringe width expression and coherent sources", "Advanced")
        add("Physics", "Unit 6: Optics", "Wave Optics", "Diffraction of light due to a single slit and central maxima width", "Advanced")
        add("Physics", "Unit 6: Optics", "Wave Optics", "Resolving power of microscope and astronomical telescope", "Medium")
        add("Physics", "Unit 6: Optics", "Wave Optics", "Polarization of light, Brewster's Law and polaroids/Malus' Law", "Medium")

        // Unit 7: Dual Nature of Matter and Radiation
        // Chapter: Dual Nature of Radiation
        add("Physics", "Unit 7: Dual Nature of Matter and Radiation", "Dual Nature of Radiation", "Photoelectric effect: Hertz and Lenard's observations", "Beginner")
        add("Physics", "Unit 7: Dual Nature of Matter and Radiation", "Dual Nature of Radiation", "Experimental study of photoelectric effect (Effect of intensity, frequency, potential)", "Medium")
        add("Physics", "Unit 7: Dual Nature of Matter and Radiation", "Dual Nature of Radiation", "Einstein's photoelectric equation and particle nature of light (Photon)", "Advanced")
        add("Physics", "Unit 7: Dual Nature of Matter and Radiation", "Dual Nature of Radiation", "Matter waves: de-Broglie relation and wavelength of electron", "Medium")
        add("Physics", "Unit 7: Dual Nature of Matter and Radiation", "Dual Nature of Radiation", "Davisson-Germer experiment (Experimental verification of wave nature)", "Medium")

        // Unit 8: Atoms and Nuclei
        // Chapter: Atoms
        add("Physics", "Unit 8: Atoms and Nuclei", "Atoms", "Alpha-particle scattering experiment and Rutherford's atomic model", "Beginner")
        add("Physics", "Unit 8: Atoms and Nuclei", "Atoms", "Bohr's model of hydrogen atom: Postulates, radii and velocity of electron", "Advanced")
        add("Physics", "Unit 8: Atoms and Nuclei", "Atoms", "Energy levels of hydrogen atom and emission/absorption line spectrum (Lyman, Balmer, Paschen)", "Advanced")

        // Chapter: Nuclei
        add("Physics", "Unit 8: Atoms and Nuclei", "Nuclei", "Composition and size of nucleus, atomic masses, isotopes, isobars, isotones", "Beginner")
        add("Physics", "Unit 8: Atoms and Nuclei", "Nuclei", "Mass-energy relation and mass defect", "Medium")
        add("Physics", "Unit 8: Atoms and Nuclei", "Nuclei", "Binding energy per nucleon and its variation with mass number", "Advanced")
        add("Physics", "Unit 8: Atoms and Nuclei", "Nuclei", "Radioactivity: Alpha, Beta, and Gamma rays and their properties", "Beginner")
        add("Physics", "Unit 8: Atoms and Nuclei", "Nuclei", "Radioactive decay law, half-life and mean life derivations", "Advanced")
        add("Physics", "Unit 8: Atoms and Nuclei", "Nuclei", "Nuclear fission, nuclear fusion and controlled chain reaction", "Medium")

        // Unit 9: Electronic Devices
        // Chapter: Semiconductor Electronics
        add("Physics", "Unit 9: Electronic Devices", "Semiconductor Electronics", "Energy bands in conductors, semiconductors and insulators", "Beginner")
        add("Physics", "Unit 9: Electronic Devices", "Semiconductor Electronics", "Intrinsic and Extrinsic semiconductors (n-type and p-type)", "Beginner")
        add("Physics", "Unit 9: Electronic Devices", "Semiconductor Electronics", "p-n junction diode formation, barrier potential and depletion layer", "Medium")
        add("Physics", "Unit 9: Electronic Devices", "Semiconductor Electronics", "I-V characteristics in forward and reverse bias", "Medium")
        add("Physics", "Unit 9: Electronic Devices", "Semiconductor Electronics", "Diode as a half-wave and full-wave rectifier", "Advanced")
        add("Physics", "Unit 9: Electronic Devices", "Semiconductor Electronics", "Special diodes: Zener diode as voltage regulator, Photodiode, LED, Solar cell", "Advanced")
        add("Physics", "Unit 9: Electronic Devices", "Semiconductor Electronics", "Junction transistor: Action and characteristics in CE configuration", "Advanced")
        add("Physics", "Unit 9: Electronic Devices", "Semiconductor Electronics", "Transistor as an amplifier (CE mode), switch and oscillator", "Advanced")
        add("Physics", "Unit 9: Electronic Devices", "Semiconductor Electronics", "Logic Gates: OR, AND, NOT, NAND, NOR truth tables and Boolean algebra", "Beginner")

        // Unit 10: Communication Systems
        // Chapter: Communication Systems
        add("Physics", "Unit 10: Communication Systems", "Communication Systems", "Elements of a communication system (Block diagram and function)", "Beginner")
        add("Physics", "Unit 10: Communication Systems", "Communication Systems", "Bandwidth of signals (Speech, TV, Digital data) and transmission medium", "Beginner")
        add("Physics", "Unit 10: Communication Systems", "Communication Systems", "Propagation of EM waves: Ground wave, Sky wave, Space wave (LOS)", "Medium")
        add("Physics", "Unit 10: Communication Systems", "Communication Systems", "Modulation: Need for modulation, Amplitude Modulation (AM) generation & detection", "Advanced")
        add("Physics", "Unit 10: Communication Systems", "Communication Systems", "Basics of satellite communication, mobile telephony, internet and remote sensing", "Beginner")

        // =========================================================================
        // 2. CHEMISTRY (Class XII)
        // =========================================================================

        // Physical Chemistry
        add("Chemistry", "Physical Chemistry", "Solid State", "Classification of solids: Crystalline vs Amorphous solids", "Beginner")
        add("Chemistry", "Physical Chemistry", "Solid State", "Crystal lattices, unit cells and calculation of atoms per unit cell", "Beginner")
        add("Chemistry", "Physical Chemistry", "Solid State", "Packing efficiency in simple cubic, bcc and fcc lattices", "Medium")
        add("Chemistry", "Physical Chemistry", "Solid State", "Voids: Tetrahedral and octahedral voids and radius ratio", "Medium")
        add("Chemistry", "Physical Chemistry", "Solid State", "Point defects: Stoichiometric (Schottky & Frenkel) and Non-stoichiometric defects", "Medium")
        add("Chemistry", "Physical Chemistry", "Solid State", "Electrical and magnetic properties of solids (Ferromagnetism, Antiferromagnetism, Ferrimagnetism)", "Beginner")

        add("Chemistry", "Physical Chemistry", "Solutions", "Concentration terms: Molarity, Molality, Mole Fraction, ppm", "Beginner")
        add("Chemistry", "Physical Chemistry", "Solutions", "Solubility of gases in liquids: Henry's Law and applications", "Beginner")
        add("Chemistry", "Physical Chemistry", "Solutions", "Raoult's Law for volatile and non-volatile solutes", "Medium")
        add("Chemistry", "Physical Chemistry", "Solutions", "Ideal and non-ideal solutions (Positive and negative deviations, Azeotropes)", "Medium")
        add("Chemistry", "Physical Chemistry", "Solutions", "Colligative properties: Relative lowering of vapour pressure", "Medium")
        add("Chemistry", "Physical Chemistry", "Solutions", "Colligative properties: Elevation of boiling point and Depression of freezing point", "Advanced")
        add("Chemistry", "Physical Chemistry", "Solutions", "Osmotic pressure, reverse osmosis and isotonic solutions", "Medium")
        add("Chemistry", "Physical Chemistry", "Solutions", "Abnormal molecular mass and Van 't Hoff factor (i)", "Advanced")

        add("Chemistry", "Physical Chemistry", "Electrochemistry", "Redox reactions and Electrochemical vs Electrolytic cells", "Beginner")
        add("Chemistry", "Physical Chemistry", "Electrochemistry", "Conductance in electrolytic solutions, specific and molar conductivity", "Medium")
        add("Chemistry", "Physical Chemistry", "Electrochemistry", "Kohlrausch's Law of independent migration of ions and applications", "Medium")
        add("Chemistry", "Physical Chemistry", "Electrochemistry", "Galvanic cells, Standard Electrode Potential and Electrochemical Series", "Medium")
        add("Chemistry", "Physical Chemistry", "Electrochemistry", "Nernst equation and EMF calculation of galvanic cells", "Advanced")
        add("Chemistry", "Physical Chemistry", "Electrochemistry", "Relationship between Gibbs free energy, Equilibrium constant and EMF", "Advanced")
        add("Chemistry", "Physical Chemistry", "Electrochemistry", "Commercial batteries: Primary (Dry cell, Mercury) and Secondary (Lead storage, Ni-Cd)", "Medium")
        add("Chemistry", "Physical Chemistry", "Electrochemistry", "Fuel cells (H2-O2 cell) and Corrosion electrochemical mechanism & prevention", "Medium")

        add("Chemistry", "Physical Chemistry", "Chemical Kinetics", "Rate of chemical reaction (Average and instantaneous rate)", "Beginner")
        add("Chemistry", "Physical Chemistry", "Chemical Kinetics", "Factors affecting reaction rate: Concentration, temperature, catalyst", "Beginner")
        add("Chemistry", "Physical Chemistry", "Chemical Kinetics", "Rate law, rate constant, Order and Molecularity of reaction", "Medium")
        add("Chemistry", "Physical Chemistry", "Chemical Kinetics", "Integrated rate equations for zero and first order reactions", "Advanced")
        add("Chemistry", "Physical Chemistry", "Chemical Kinetics", "Half-life of a reaction and pseudo first-order reactions", "Medium")
        add("Chemistry", "Physical Chemistry", "Chemical Kinetics", "Arrhenius equation, Activation energy and temperature dependence of rate constant", "Advanced")
        add("Chemistry", "Physical Chemistry", "Chemical Kinetics", "Collision theory of chemical reactions and steric factor", "Medium")

        add("Chemistry", "Physical Chemistry", "Surface Chemistry", "Adsorption: Physisorption vs Chemisorption characteristics", "Beginner")
        add("Chemistry", "Physical Chemistry", "Surface Chemistry", "Freundlich adsorption isotherm", "Medium")
        add("Chemistry", "Physical Chemistry", "Surface Chemistry", "Catalysis: Homogeneous, heterogeneous and enzyme catalysis", "Medium")
        add("Chemistry", "Physical Chemistry", "Surface Chemistry", "Colloids: Lyophilic vs Lyophobic, Multimolecular, Macromolecular, Associated (Micelles)", "Beginner")
        add("Chemistry", "Physical Chemistry", "Surface Chemistry", "Properties of colloids: Tyndall effect, Brownian motion, Electrophoresis, Hardy-Schulze Rule", "Medium")
        add("Chemistry", "Physical Chemistry", "Surface Chemistry", "Emulsions: Types and preparation/applications", "Beginner")

        // Inorganic Chemistry
        add("Chemistry", "Inorganic Chemistry", "General Principles of Isolation", "Principles and methods of extraction: Concentration of ores (Froth flotation, Leaching)", "Medium")
        add("Chemistry", "Inorganic Chemistry", "General Principles of Isolation", "Oxidation, reduction (Smelting, Calcination, Roasting) and Ellingham diagrams", "Advanced")
        add("Chemistry", "Inorganic Chemistry", "General Principles of Isolation", "Refining of metals (Liquation, Zone refining, Mond process, Van Arkel method)", "Medium")

        add("Chemistry", "Inorganic Chemistry", "p-Block Elements", "Group 15 Elements: Electronic config, oxidation states, anomalous behavior of Nitrogen", "Beginner")
        add("Chemistry", "Inorganic Chemistry", "p-Block Elements", "Dinitrogen, Ammonia (Haber process), Nitric acid (Ostwald process) and oxides of nitrogen", "Medium")
        add("Chemistry", "Inorganic Chemistry", "p-Block Elements", "Phosphorus allotropes, Phosphine, Halides and Oxoacids of Phosphorus", "Medium")
        add("Chemistry", "Inorganic Chemistry", "p-Block Elements", "Group 16 Elements: Dioxygen, Ozone, Sulphur allotropes, SO2, Sulphuric acid (Contact process)", "Medium")
        add("Chemistry", "Inorganic Chemistry", "p-Block Elements", "Group 17 Elements (Halogens): Chlorine, HCl, Interhalogen compounds, Oxoacids of Halogens", "Medium")
        add("Chemistry", "Inorganic Chemistry", "p-Block Elements", "Group 18 Elements (Noble gases): Electronic config, Xenon fluorides and oxides structure", "Advanced")

        add("Chemistry", "Inorganic Chemistry", "d- and f-Block Elements", "Transition elements: Electronic configuration, variable oxidation states, metallic character", "Beginner")
        add("Chemistry", "Inorganic Chemistry", "d- and f-Block Elements", "Magnetic properties, catalytic properties, interstitial compounds, alloy formation", "Medium")
        add("Chemistry", "Inorganic Chemistry", "d- and f-Block Elements", "Lanthanoids: Electronic configuration, oxidation states and Lanthanoid contraction consequences", "Advanced")
        add("Chemistry", "Inorganic Chemistry", "d- and f-Block Elements", "Actinoids: Electronic config, oxidation states and comparison with Lanthanoids", "Medium")
        add("Chemistry", "Inorganic Chemistry", "d- and f-Block Elements", "Preparation, properties and oxidizing actions of K2Cr2O7 and KMnO4", "Advanced")

        add("Chemistry", "Inorganic Chemistry", "Coordination Compounds", "Werner's coordination theory and coordination entity, central atom, ligands, coordination number", "Beginner")
        add("Chemistry", "Inorganic Chemistry", "Coordination Compounds", "IUPAC nomenclature of mononuclear coordination complexes", "Medium")
        add("Chemistry", "Inorganic Chemistry", "Coordination Compounds", "Isomerism in coordination compounds (Geometrical, Optical, Structural)", "Medium")
        add("Chemistry", "Inorganic Chemistry", "Coordination Compounds", "Valence Bond Theory (VBT): Hybridization, geometry, magnetic behavior", "Advanced")
        add("Chemistry", "Inorganic Chemistry", "Coordination Compounds", "Crystal Field Theory (CFT): Crystal field splitting in octahedral and tetrahedral fields", "Advanced")
        add("Chemistry", "Inorganic Chemistry", "Coordination Compounds", "Bonding in metal carbonyls and applications in qualitative analysis/medicine", "Medium")

        // Organic Chemistry
        add("Chemistry", "Organic Chemistry", "Haloalkanes and Haloarenes", "Nomenclature and nature of C-X bond", "Beginner")
        add("Chemistry", "Organic Chemistry", "Haloalkanes and Haloarenes", "Methods of preparation from alcohols, hydrocarbons, and halogen exchange (Finkelstein/Swarts)", "Medium")
        add("Chemistry", "Organic Chemistry", "Haloalkanes and Haloarenes", "Nucleophilic substitution mechanisms: SN1 and SN2 pathways and stereochemical aspects", "Advanced")
        add("Chemistry", "Organic Chemistry", "Haloalkanes and Haloarenes", "Elimination reactions (Saytzeff rule) and reactions with metals (Grignard, Wurtz)", "Medium")
        add("Chemistry", "Organic Chemistry", "Haloalkanes and Haloarenes", "Reactions of haloarenes (Low reactivity towards nucleophilic substitution & electrophilic reactions)", "Medium")
        add("Chemistry", "Organic Chemistry", "Haloalkanes and Haloarenes", "Polyhalogen compounds: Chloroform, Iodoform, Freons, DDT environmental effects", "Beginner")

        add("Chemistry", "Organic Chemistry", "Alcohols, Phenols and Ethers", "Classification and IUPAC nomenclature of Alcohols, Phenols, and Ethers", "Beginner")
        add("Chemistry", "Organic Chemistry", "Alcohols, Phenols and Ethers", "Preparation of alcohols (Hydration, Hydroboration-oxidation, Reduction of carbonyls)", "Medium")
        add("Chemistry", "Organic Chemistry", "Alcohols, Phenols and Ethers", "Preparation of phenols from cumene, chlorobenzene (Dow's process), diazonium salts", "Medium")
        add("Chemistry", "Organic Chemistry", "Alcohols, Phenols and Ethers", "Physical and chemical properties: Acidity of alcohols and phenols comparison", "Medium")
        add("Chemistry", "Organic Chemistry", "Alcohols, Phenols and Ethers", "Dehydration mechanism of alcohols to alkenes/ethers", "Advanced")
        add("Chemistry", "Organic Chemistry", "Alcohols, Phenols and Ethers", "Electrophilic substitution of phenols: Reimer-Tiemann reaction and Kolbe's reaction", "Advanced")
        add("Chemistry", "Organic Chemistry", "Alcohols, Phenols and Ethers", "Ethers: Williamson synthesis and cleavage of C-O bond by HI mechanism", "Advanced")

        add("Chemistry", "Organic Chemistry", "Aldehydes, Ketones and Carboxylic Acids", "Nomenclature and structure of carbonyl group", "Beginner")
        add("Chemistry", "Organic Chemistry", "Aldehydes, Ketones and Carboxylic Acids", "Preparation methods (Rosenmund reduction, Stephen's reduction, Etard reaction, Friedel-Crafts)", "Medium")
        add("Chemistry", "Organic Chemistry", "Aldehydes, Ketones and Carboxylic Acids", "Nucleophilic addition reactions with HCN, NaHSO3, Grignard, Alcohols, Ammonia derivatives", "Advanced")
        add("Chemistry", "Organic Chemistry", "Aldehydes, Ketones and Carboxylic Acids", "Distinguishing tests: Tollens' reagent, Fehling's solution, Iodoform test", "Medium")
        add("Chemistry", "Organic Chemistry", "Aldehydes, Ketones and Carboxylic Acids", "Named reactions: Aldol condensation, Cross aldol, Cannizzaro reaction, Clemmensen/Wolff-Kishner reduction", "Advanced")
        add("Chemistry", "Organic Chemistry", "Aldehydes, Ketones and Carboxylic Acids", "Carboxylic acids: Preparation methods and acidic nature (Inductive and resonance effects)", "Medium")
        add("Chemistry", "Organic Chemistry", "Aldehydes, Ketones and Carboxylic Acids", "Reactions involving -COOH group: Esterification, HVZ reaction, Decarboxylation", "Advanced")

        add("Chemistry", "Organic Chemistry", "Amines", "Classification and IUPAC nomenclature of primary, secondary, and tertiary amines", "Beginner")
        add("Chemistry", "Organic Chemistry", "Amines", "Methods of preparation: Gabriel phthalimide synthesis, Hoffmann bromamide degradation", "Advanced")
        add("Chemistry", "Organic Chemistry", "Amines", "Basicity of amines in gas phase and aqueous medium", "Medium")
        add("Chemistry", "Organic Chemistry", "Amines", "Chemical reactions: Carbylamine test, Reaction with nitrous acid, Hinsberg test", "Medium")
        add("Chemistry", "Organic Chemistry", "Amines", "Diazonium salts: Preparation (Diazotization) and synthetic applications (Sandmeyer, Gattermann)", "Advanced")

        add("Chemistry", "Organic Chemistry", "Biomolecules", "Carbohydrates: Classification (Monosaccharides, Oligosaccharides, Polysaccharides)", "Beginner")
        add("Chemistry", "Organic Chemistry", "Biomolecules", "Structure and reactions of D-Glucose and Fructose", "Medium")
        add("Chemistry", "Organic Chemistry", "Biomolecules", "Disaccharides (Sucrose, Lactose, Maltose) and Polysaccharides (Starch, Cellulose, Glycogen)", "Medium")
        add("Chemistry", "Organic Chemistry", "Biomolecules", "Amino acids: Classification, Essential/Non-essential, Zwitterion structure, Isoelectric point", "Beginner")
        add("Chemistry", "Organic Chemistry", "Biomolecules", "Proteins: Peptide bond, Primary, Secondary (alpha-helix, beta-pleated sheet), Tertiary, Quaternary structures", "Medium")
        add("Chemistry", "Organic Chemistry", "Biomolecules", "Denaturation of proteins and biological functions of enzymes", "Beginner")
        add("Chemistry", "Organic Chemistry", "Biomolecules", "Nucleic acids: Chemical composition, DNA and RNA structure, Double helix model, Replication", "Medium")
        add("Chemistry", "Organic Chemistry", "Biomolecules", "Vitamins: Classification (Fat-soluble & Water-soluble) and deficiency diseases", "Beginner")

        add("Chemistry", "Organic Chemistry", "Polymers", "Classification of polymers based on source, structure, and molecular forces", "Beginner")
        add("Chemistry", "Organic Chemistry", "Polymers", "Types of polymerization: Addition (Free radical) and Condensation polymerization", "Medium")
        add("Chemistry", "Organic Chemistry", "Polymers", "Preparation and uses of Polythene, Nylon 6,6, Nylon 6, Dacron (Terylene), Bakelite, Melamine", "Medium")
        add("Chemistry", "Organic Chemistry", "Polymers", "Natural and synthetic rubbers (Buna-S, Buna-N, Neoprene) and Vulcanization of rubber", "Medium")
        add("Chemistry", "Organic Chemistry", "Polymers", "Biodegradable polymers: PHBV and Nylon-2-Nylon-6", "Beginner")

        add("Chemistry", "Organic Chemistry", "Chemistry in Everyday Life", "Drugs and classification: Analgesics (Narcotic & Non-narcotic), Antipyretics, Tranquilizers", "Beginner")
        add("Chemistry", "Organic Chemistry", "Chemistry in Everyday Life", "Antiseptics, Disinfectants, Antibiotics (Broad spectrum & Narrow spectrum), Antacids, Antihistamines", "Medium")
        add("Chemistry", "Organic Chemistry", "Chemistry in Everyday Life", "Chemicals in food: Artificial sweetening agents (Aspartame, Saccharin) and Preservatives", "Beginner")
        add("Chemistry", "Organic Chemistry", "Chemistry in Everyday Life", "Cleansing agents: Soaps (Saponification) and Synthetic detergents (Anionic, Cationic, Non-ionic)", "Medium")

        // =========================================================================
        // 3. BIOLOGY (Class XII) - 5 Units
        // =========================================================================

        // Unit 1: Reproduction
        add("Biology", "Unit 1: Reproduction", "Reproduction in Organisms", "Asexual and sexual modes of reproduction", "Beginner")
        add("Biology", "Unit 1: Reproduction", "Reproduction in Organisms", "Vegetative propagation in angiosperms and life spans", "Beginner")
        add("Biology", "Unit 1: Reproduction", "Sexual Reproduction in Flowering Plants", "Flower structure and development of male & female gametophytes (Micro/Megasporogenesis)", "Medium")
        add("Biology", "Unit 1: Reproduction", "Sexual Reproduction in Flowering Plants", "Pollination: Types (Autogamy, Geitonogamy, Xenogamy), agents and outbreeding devices", "Medium")
        add("Biology", "Unit 1: Reproduction", "Sexual Reproduction in Flowering Plants", "Pollen-pistil interaction and double fertilization", "Advanced")
        add("Biology", "Unit 1: Reproduction", "Sexual Reproduction in Flowering Plants", "Endosperm and embryo development, seed and fruit formation", "Medium")
        add("Biology", "Unit 1: Reproduction", "Sexual Reproduction in Flowering Plants", "Apomixis, Parthenocarpy and Polyembryony", "Beginner")
        add("Biology", "Unit 1: Reproduction", "Human Reproduction", "Male reproductive system: Anatomy and histology of Testis", "Beginner")
        add("Biology", "Unit 1: Reproduction", "Human Reproduction", "Female reproductive system: Anatomy and histology of Ovary", "Beginner")
        add("Biology", "Unit 1: Reproduction", "Human Reproduction", "Gametogenesis: Spermatogenesis vs Oogenesis and hormonal regulation", "Advanced")
        add("Biology", "Unit 1: Reproduction", "Human Reproduction", "Menstrual cycle and ovarian/uterine phases", "Advanced")
        add("Biology", "Unit 1: Reproduction", "Human Reproduction", "Fertilization, cleavage, blastocyst formation and implantation", "Medium")
        add("Biology", "Unit 1: Reproduction", "Human Reproduction", "Pregnancy, placenta formation, parturition and lactation", "Medium")
        add("Biology", "Unit 1: Reproduction", "Reproductive Health", "Need for reproductive health, population explosion and birth control methods (Contraception)", "Beginner")
        add("Biology", "Unit 1: Reproduction", "Reproductive Health", "Medical Termination of Pregnancy (MTP) and Sexually Transmitted Infections (STIs)", "Beginner")
        add("Biology", "Unit 1: Reproduction", "Reproductive Health", "Infertility and Assisted Reproductive Technologies (ART): IVF, ET, ZIFT, GIFT, ICSI, IUI", "Medium")

        // Unit 2: Genetics and Evolution
        add("Biology", "Unit 2: Genetics and Evolution", "Heredity and Variation", "Mendelian inheritance: Monohybrid and Dihybrid crosses, Laws of Inheritance", "Beginner")
        add("Biology", "Unit 2: Genetics and Evolution", "Heredity and Variation", "Deviations from Mendelism: Incomplete dominance, Codominance, Multiple alleles (ABO blood groups)", "Medium")
        add("Biology", "Unit 2: Genetics and Evolution", "Heredity and Variation", "Chromosomal theory of inheritance, Linkage and Recombination (Morgan's experiments)", "Advanced")
        add("Biology", "Unit 2: Genetics and Evolution", "Heredity and Variation", "Sex determination mechanisms in humans, birds, and honeybees", "Beginner")
        add("Biology", "Unit 2: Genetics and Evolution", "Heredity and Variation", "Mutation and pedigree analysis", "Medium")
        add("Biology", "Unit 2: Genetics and Evolution", "Heredity and Variation", "Mendelian disorders (Thalassemia, Hemophilia, Sickle-cell anemia, Phenylketonuria)", "Advanced")
        add("Biology", "Unit 2: Genetics and Evolution", "Heredity and Variation", "Chromosomal disorders (Down's syndrome, Turner's syndrome, Klinefelter's syndrome)", "Medium")

        add("Biology", "Unit 2: Genetics and Evolution", "Molecular Basis of Inheritance", "DNA as genetic material: Griffith, Avery-MacLeod-McCarty, Hershey-Chase experiments", "Beginner")
        add("Biology", "Unit 2: Genetics and Evolution", "Molecular Basis of Inheritance", "Structure of DNA and RNA and packaging of DNA helix (Nucleosome)", "Medium")
        add("Biology", "Unit 2: Genetics and Evolution", "Molecular Basis of Inheritance", "DNA replication: Meselson-Stahl experiment and replication fork machinery", "Advanced")
        add("Biology", "Unit 2: Genetics and Evolution", "Molecular Basis of Inheritance", "Transcription: Transcription unit, promoter, RNA polymerase in prokaryotes & eukaryotes", "Advanced")
        add("Biology", "Unit 2: Genetics and Evolution", "Molecular Basis of Inheritance", "Genetic Code: Salient features, wobble hypothesis, tRNA adaptor molecule", "Medium")
        add("Biology", "Unit 2: Genetics and Evolution", "Molecular Basis of Inheritance", "Translation: Process of protein synthesis (Initiation, Elongation, Termination)", "Advanced")
        add("Biology", "Unit 2: Genetics and Evolution", "Molecular Basis of Inheritance", "Regulation of gene expression: Lac Operon model", "Advanced")
        add("Biology", "Unit 2: Genetics and Evolution", "Molecular Basis of Inheritance", "Human Genome Project (HGP): Goals, methodologies, salient features", "Medium")
        add("Biology", "Unit 2: Genetics and Evolution", "Molecular Basis of Inheritance", "DNA Fingerprinting: Principle, VNTRs, methodology and applications", "Advanced")

        add("Biology", "Unit 2: Genetics and Evolution", "Evolution", "Origin of life: Oparin-Haldane theory and Miller-Urey experiment", "Beginner")
        add("Biology", "Unit 2: Genetics and Evolution", "Evolution", "Evidences of evolution: Paleontological, homologous and analogous organs, embryological", "Medium")
        add("Biology", "Unit 2: Genetics and Evolution", "Evolution", "Darwinian theory of natural selection and Lamarckism", "Beginner")
        add("Biology", "Unit 2: Genetics and Evolution", "Evolution", "Modern synthetic theory of evolution and Hugo de Vries mutation theory", "Medium")
        add("Biology", "Unit 2: Genetics and Evolution", "Evolution", "Hardy-Weinberg Principle and factors affecting genetic equilibrium", "Advanced")
        add("Biology", "Unit 2: Genetics and Evolution", "Evolution", "Adaptive radiation (Darwin's finches, Australian marsupials) and Biological evolution mechanism", "Medium")
        add("Biology", "Unit 2: Genetics and Evolution", "Evolution", "Human evolution: Dryopithecus, Australopithecus, Homo habilis, Homo erectus, Neanderthal, Homo sapiens", "Medium")

        // Unit 3: Biology in Human Welfare
        add("Biology", "Unit 3: Biology in Human Welfare", "Human Health and Diseases", "Common infectious diseases in humans: Malaria, Typhoid, Pneumonia, Amoebiasis, Ascariasis, Ringworm", "Beginner")
        add("Biology", "Unit 3: Biology in Human Welfare", "Human Health and Diseases", "Immunity: Innate immunity vs Acquired immunity, Humoral and Cell-mediated response", "Medium")
        add("Biology", "Unit 3: Biology in Human Welfare", "Human Health and Diseases", "Antigens, Antibodies (Structure of IgG), Active and passive immunity, Vaccines", "Medium")
        add("Biology", "Unit 3: Biology in Human Welfare", "Human Health and Diseases", "Allergies, Autoimmunity (Rheumatoid arthritis) and Immune system organs (Primary & Secondary)", "Beginner")
        add("Biology", "Unit 3: Biology in Human Welfare", "Human Health and Diseases", "AIDS: Causative agent (HIV structure & replication), transmission, diagnosis (ELISA) & prevention", "Advanced")
        add("Biology", "Unit 3: Biology in Human Welfare", "Human Health and Diseases", "Cancer: Types, causes (Carcinogens), oncogenes, diagnosis and treatment modalities", "Advanced")
        add("Biology", "Unit 3: Biology in Human Welfare", "Human Health and Diseases", "Adolescence and drug/alcohol abuse: Opioids, Cannabinoids, Coca alkaloids, addiction and rehabilitation", "Beginner")

        add("Biology", "Unit 3: Biology in Human Welfare", "Strategies for Food Production", "Animal husbandry: Dairy, poultry farm management, animal breeding (Inbreeding & Outbreeding, MOET)", "Medium")
        add("Biology", "Unit 3: Biology in Human Welfare", "Strategies for Food Production", "Plant breeding: Steps in developing high-yielding, disease-resistant and pest-resistant varieties", "Medium")
        add("Biology", "Unit 3: Biology in Human Welfare", "Strategies for Food Production", "Biofortification, Single Cell Protein (SCP) and Tissue culture (Micropropagation, Somatic hybrids)", "Beginner")

        add("Biology", "Unit 3: Biology in Human Welfare", "Microbes in Human Welfare", "Microbes in household food processing (Lactobacillus, Yeast, Cheese making)", "Beginner")
        add("Biology", "Unit 3: Biology in Human Welfare", "Microbes in Human Welfare", "Microbes in industrial production (Beverages, Antibiotics, Organic acids, Enzymes, Cyclosporin A, Statins)", "Medium")
        add("Biology", "Unit 3: Biology in Human Welfare", "Microbes in Human Welfare", "Microbes in sewage treatment (Primary & Secondary biological treatment, Activated sludge)", "Medium")
        add("Biology", "Unit 3: Biology in Human Welfare", "Microbes in Human Welfare", "Microbes in biogas production (Methanogens) and Biocontrol agents (Bt, Trichoderma, Baculoviruses)", "Beginner")
        add("Biology", "Unit 3: Biology in Human Welfare", "Microbes in Human Welfare", "Microbes as biofertilizers (Rhizobium, Azospirillum, Mycorrhiza, Cyanobacteria)", "Beginner")

        // Unit 4: Biotechnology and Its Applications
        add("Biology", "Unit 4: Biotechnology and Its Applications", "Principles and Processes of Biotechnology", "Principles of biotechnology: Genetic engineering and bioprocess engineering", "Beginner")
        add("Biology", "Unit 4: Biotechnology and Its Applications", "Principles and Processes of Biotechnology", "Tools of recombinant DNA technology: Restriction endonucleases, Ligases, Polymerases", "Advanced")
        add("Biology", "Unit 4: Biotechnology and Its Applications", "Principles and Processes of Biotechnology", "Cloning vectors: Features of pBR322 vector (ori, selectable markers, rop, cloning sites)", "Advanced")
        add("Biology", "Unit 4: Biotechnology and Its Applications", "Principles and Processes of Biotechnology", "Competent host transformation methods (Heat shock, Microinjection, Gene gun/Biolistics)", "Medium")
        add("Biology", "Unit 4: Biotechnology and Its Applications", "Principles and Processes of Biotechnology", "Processes of recombinant DNA technology: Isolation of DNA, PCR amplification (Denaturation, Annealing, Extension)", "Advanced")
        add("Biology", "Unit 4: Biotechnology and Its Applications", "Principles and Processes of Biotechnology", "Bioreactors (Stirred-tank & Sparged) and Downstream processing", "Medium")

        add("Biology", "Unit 4: Biotechnology and Its Applications", "Applications of Biotechnology", "Biotechnological applications in agriculture: Bt Cotton (Cry proteins) and RNA interference (RNAi) in tobacco", "Advanced")
        add("Biology", "Unit 4: Biotechnology and Its Applications", "Applications of Biotechnology", "Biotechnology in medicine: Genetically engineered insulin (Humulin preparation)", "Advanced")
        add("Biology", "Unit 4: Biotechnology and Its Applications", "Applications of Biotechnology", "Gene therapy (ADA deficiency treatment) and Molecular diagnosis (PCR, ELISA)", "Medium")
        add("Biology", "Unit 4: Biotechnology and Its Applications", "Applications of Biotechnology", "Transgenic animals: Production reasons and safety testing", "Beginner")
        add("Biology", "Unit 4: Biotechnology and Its Applications", "Applications of Biotechnology", "Ethical issues, GEAC role, patents and Biopiracy (Basmati rice, Neem)", "Beginner")

        // Unit 5: Ecology and Environment
        add("Biology", "Unit 5: Ecology and Environment", "Organisms and Populations", "Organism and its environment: Major abiotic factors (Temperature, Water, Light, Soil)", "Beginner")
        add("Biology", "Unit 5: Ecology and Environment", "Organisms and Populations", "Responses to abiotic factors: Regulators, conformers, migration and suspension", "Medium")
        add("Biology", "Unit 5: Ecology and Environment", "Organisms and Populations", "Adaptations: Morphological, physiological and behavioral adaptations in plants and animals", "Beginner")
        add("Biology", "Unit 5: Ecology and Environment", "Organisms and Populations", "Population attributes: Natality, mortality, sex ratio, age pyramids", "Medium")
        add("Biology", "Unit 5: Ecology and Environment", "Organisms and Populations", "Population growth models: Exponential growth vs Logistic growth (Verhulst-Pearl curve)", "Advanced")
        add("Biology", "Unit 5: Ecology and Environment", "Organisms and Populations", "Population interactions: Mutualism, Competition (Gause's principle), Predation, Parasitism, Commensalism, Amensalism", "Medium")

        add("Biology", "Unit 5: Ecology and Environment", "Ecosystem", "Ecosystem structure and function: Productivity (Primary & Secondary, GPP, NPP)", "Beginner")
        add("Biology", "Unit 5: Ecology and Environment", "Ecosystem", "Decomposition process (Fragmentation, Leaching, Catabolism, Humification, Mineralization)", "Medium")
        add("Biology", "Unit 5: Ecology and Environment", "Ecosystem", "Energy flow: Food chain, food web, 10% law of energy transfer", "Beginner")
        add("Biology", "Unit 5: Ecology and Environment", "Ecosystem", "Ecological pyramids: Pyramid of number, biomass, and energy (Upright & Inverted)", "Medium")
        add("Biology", "Unit 5: Ecology and Environment", "Ecosystem", "Ecological succession: Primary vs Secondary succession (Hydrarch & Xerarch succession)", "Medium")
        add("Biology", "Unit 5: Ecology and Environment", "Ecosystem", "Nutrient cycling: Carbon cycle and Phosphorus cycle (Sedimentary vs Gaseous)", "Beginner")
        add("Biology", "Unit 5: Ecology and Environment", "Ecosystem", "Ecosystem services: Robert Costanza valuation of ecosystem services", "Beginner")

        add("Biology", "Unit 5: Ecology and Environment", "Biodiversity and Conservation", "Concepts and levels of biodiversity: Genetic, species and ecological diversity", "Beginner")
        add("Biology", "Unit 5: Ecology and Environment", "Biodiversity and Conservation", "Patterns of biodiversity: Latitudinal gradient and Species-Area relationship (Alexander von Humboldt)", "Medium")
        add("Biology", "Unit 5: Ecology and Environment", "Biodiversity and Conservation", "Importance of biodiversity: Rivet popper hypothesis (Paul Ehrlich)", "Beginner")
        add("Biology", "Unit 5: Ecology and Environment", "Biodiversity and Conservation", "Loss of biodiversity: 'The Evil Quartet' causes (Habitat loss, Over-exploitation, Alien invasion, Co-extinctions)", "Medium")
        add("Biology", "Unit 5: Ecology and Environment", "Biodiversity and Conservation", "Biodiversity conservation strategies: In-situ (National parks, Sanctuaries, Biosphere reserves, Sacred groves) vs Ex-situ (Zoological parks, Botanical gardens, Cryopreservation)", "Medium")

        add("Biology", "Unit 5: Ecology and Environment", "Environmental Issues", "Air pollution and control: Electrostatic precipitator, Catalytic converters, CNG in Delhi", "Medium")
        add("Biology", "Unit 5: Ecology and Environment", "Environmental Issues", "Water pollution and control: BOD, Biomagnification (DDT), Eutrophication, Algal blooms", "Medium")
        add("Biology", "Unit 5: Ecology and Environment", "Environmental Issues", "Solid waste management: Sanitary landfills, e-waste, plastic waste recycling (Ahmed Khan case study)", "Beginner")
        add("Biology", "Unit 5: Ecology and Environment", "Environmental Issues", "Agrochemicals and organic farming (Ramesh Chandra Dagar integrated organic farming)", "Beginner")
        add("Biology", "Unit 5: Ecology and Environment", "Environmental Issues", "Greenhouse effect and global warming, Ozone depletion in stratosphere (Montreal Protocol), Deforestation (Amrita Devi Bishnoi award)", "Medium")

        // =========================================================================
        // 4. HINDI (Digant Part-2) & 5. ENGLISH (Class XII)
        // =========================================================================
        add("Hindi", "Literature: Digant Part-2 (Prose)", "बातचीत (बालकृष्ण भट्ट)", "बातचीत की शैली और आत्म-संवाद", "Beginner")
        add("Hindi", "Literature: Digant Part-2 (Prose)", "उसने कहा था (चन्द्रधर शर्मा गुलेरी)", "लहना सिंह का चरित्र-चित्रण और त्याग", "Medium")
        add("Hindi", "Literature: Digant Part-2 (Prose)", "सम्पूर्ण क्रांति (जयप्रकाश नारायण)", "छात्र आंदोलन और लोकतांत्रिक चेतना", "Beginner")
        add("Hindi", "Literature: Digant Part-2 (Prose)", "अर्धनारीश्वर (रामधारी सिंह 'दिनकर')", "स्त्री-पुरुष समानता और दार्शनिक विचार", "Medium")
        add("Hindi", "Literature: Digant Part-2 (Prose)", "रोज़ (अज्ञेय)", "मालती का दैनिक जीवन और मध्यवर्गीय त्रासदी", "Medium")
        add("Hindi", "Literature: Digant Part-2 (Prose)", "एक लेख और एक पत्र (भगत सिंह)", "क्रांतिकारी विचार और शहादत की प्रेरणा", "Beginner")
        add("Hindi", "Literature: Digant Part-2 (Prose)", "ओ सदानीरा (जगदीशचंद्र माथुर)", "गंडक नदी और चंपारण की ऐतिहासिक संस्कृति", "Advanced")
        add("Hindi", "Literature: Digant Part-2 (Prose)", "सिपाही की माँ (मोहन राकेश)", "एकांकी शिल्प और युद्ध की अमानवीयता", "Medium")
        add("Hindi", "Literature: Digant Part-2 (Prose)", "प्रगीत और समाज (नामवर सिंह)", "आधुनिक कविता और समाजशास्त्रीय दृष्टिकोण", "Advanced")
        add("Hindi", "Literature: Digant Part-2 (Prose)", "जूठन (ओमप्रकाश वाल्मीकि)", "दलित आत्मकथा और सामाजिक शोषण का यथार्थ", "Medium")
        add("Hindi", "Literature: Digant Part-2 (Prose)", "हँसते हुए मेरा अकेलापन (मलयज)", "डायरी विधा और आंतरिक एकांत", "Medium")
        add("Hindi", "Literature: Digant Part-2 (Prose)", "तिरिछ (उदय प्रकाश)", "जादुई यथार्थवाद और आधुनिक भय", "Advanced")
        add("Hindi", "Literature: Digant Part-2 (Prose)", "शिक्षा (जे. कृष्णमूर्ति)", "सच्ची शिक्षा और मन की स्वतंत्रता", "Beginner")

        add("Hindi", "Literature: Digant Part-2 (Poetry)", "कड़बक (मलिक मोहम्मद जायसी)", "प्रेम की पीर और रूप-सौंदर्य की नश्वरता", "Beginner")
        add("Hindi", "Literature: Digant Part-2 (Poetry)", "पद (सूरदास)", "वात्सल्य रस और बालकृष्ण की चेष्टाएँ", "Medium")
        add("Hindi", "Literature: Digant Part-2 (Poetry)", "पद (तुलसीदास)", "भक्ति भावना और दीनता का भाव", "Medium")
        add("Hindi", "Literature: Digant Part-2 (Poetry)", "छप्पय (नाभादास)", "भक्त कवियों का मूल्यांकन", "Medium")
        add("Hindi", "Literature: Digant Part-2 (Poetry)", "कवित्त (भूषण)", "वीर रस और छत्रपति शिवाजी / छत्रसाल की वीरता", "Beginner")
        add("Hindi", "Literature: Digant Part-2 (Poetry)", "तुमुल कोलाहल कलह में (जयशंकर प्रसाद)", "कामायनी का 'इड़ा' प्रसंग और चेतना", "Advanced")
        add("Hindi", "Literature: Digant Part-2 (Poetry)", "पुत्र वियोग (सुभद्रा कुमारी चौहान)", "मातृत्व का करुण विलाप", "Beginner")
        add("Hindi", "Literature: Digant Part-2 (Poetry)", "उषा (शमशेर बहादुर सिंह)", "प्रातःकालीन सौंदर्य और बिंब योजना", "Medium")
        add("Hindi", "Literature: Digant Part-2 (Poetry)", "जन-जन का चेहरा एक (मुक्तिबोध)", "वैश्विक शोषित वर्ग का संघर्ष", "Advanced")
        add("Hindi", "Literature: Digant Part-2 (Poetry)", "अधिनायक (रघुवीर सहाय)", "समकालीन राजनीति पर तीखा व्यंग्य", "Medium")
        add("Hindi", "Literature: Digant Part-2 (Poetry)", "प्यारे नन्हें बेटे को (विनोद कुमार शुक्ल)", "मेहनतकश इंसान और लोहे का प्रतीक", "Medium")
        add("Hindi", "Literature: Digant Part-2 (Poetry)", "हार-जीत (अशोक वाजपेयी)", "गद्य कविता और विजय उत्सव का भ्रम", "Beginner")
        add("Hindi", "Literature: Digant Part-2 (Poetry)", "गाँव का घर (ज्ञानेंद्रपति)", "खोती हुई ग्रामीण संस्कृति और आधुनिकता", "Medium")

        add("Hindi", "Grammar: Vyakaran & Rachna", "संधि एवं समास", "स्वर, व्यंजन, विसर्ग संधि तथा 6 समास भेद", "Beginner")
        add("Hindi", "Grammar: Vyakaran & Rachna", "पदबंध एवं वाक्य रूपांतरण", "संज्ञा, विशेषण, क्रिया पदबंध व सरल/संयुक्त/मिश्र वाक्य", "Medium")
        add("Hindi", "Grammar: Vyakaran & Rachna", "मुहावरे एवं लोकोक्तियाँ", "महत्वपूर्ण परीक्षा-उपयोगी मुहावरे व सटीक वाक्य प्रयोग", "Beginner")
        add("Hindi", "Grammar: Vyakaran & Rachna", "उपसर्ग एवं प्रत्यय", "संस्कृत, हिंदी तथा आगत उपसर्ग-प्रत्यय", "Beginner")
        add("Hindi", "Grammar: Vyakaran & Rachna", "निबंध एवं पत्र लेखन", "समसामयिक विषयों पर निबंध व औपचारिक/अनौपचारिक पत्र", "Medium")
        add("Hindi", "Grammar: Vyakaran & Rachna", "संक्षेपण (Précis Writing)", "एक-तिहाई शब्दों में शीर्षक सहित संक्षेपण कौशल", "Advanced")

        // English
        add("English", "Section A: Reading", "Unseen Comprehension", "Factual & Discursive Passages with Inferences", "Medium")
        add("English", "Section A: Reading", "Note-Making & Summarization", "Systematic Note-making with Abbreviations & Summary", "Advanced")
        add("English", "Section B: Writing Skills", "Short Compositions", "Notice Writing, Advertisements & Formal Invitations", "Beginner")
        add("English", "Section B: Writing Skills", "Extended Writing", "Official Applications, Letter to Editor & Job Application (CV)", "Medium")
        add("English", "Section B: Writing Skills", "Discursive & Analytical Writing", "Essay & Article Writing on Contemporary Science & Society", "Advanced")
        add("English", "Section B: Writing Skills", "Précis Writing", "Summary and Central Theme Condensation", "Medium")
        add("English", "Section C: Grammar & Usage", "Tenses & Voice", "Narrative Contexts & Active/Passive Voice Transformations", "Beginner")
        add("English", "Section C: Grammar & Usage", "Reported Speech", "Direct to Indirect Transformation Rules", "Medium")
        add("English", "Section C: Grammar & Usage", "Sentence Synthesis & Clauses", "Combining Sentences using Relative/Adverb Clauses", "Advanced")
        add("English", "Section C: Grammar & Usage", "Modal Auxiliaries & Phrasal Verbs", "Contextual Vocabulary, Idioms & Prepositional Phrases", "Medium")
        add("English", "Section C: Grammar & Usage", "Translation Skills", "Hindi to English and English to Hindi Translation", "Medium")
        add("English", "Section D: Literature", "Prose & Themes", "Critical Analysis of Prescribed Short Stories & Essays", "Medium")
        add("English", "Section D: Literature", "Poetic Devices & Figures of Speech", "Metaphor, Simile, Imagery & Symbolism in Modern Poetry", "Advanced")

        return list
    }

    fun getSubjects(): List<String> = listOf("Physics", "Chemistry", "Biology", "Hindi", "English")
    fun getPcbSubjects(): List<String> = listOf("Physics", "Chemistry", "Biology")

    fun getSyllabusSubjects(board: String = "BSEB"): List<SubjectEntity> {
        val topics = getDefaultSyllabus(board)
        val subjectOrder = listOf("Physics", "Chemistry", "Biology", "Hindi", "English")
        val colorMap = mapOf(
            "Physics" to "#06B6D4",
            "Chemistry" to "#10B981",
            "Biology" to "#F59E0B",
            "Hindi" to "#EC4899",
            "English" to "#3B82F6"
        )

        return topics.groupBy { it.subject }.map { (subjectName, subTopics) ->
            val unitsCount = subTopics.map { it.unitName }.distinct().size
            val chaptersCount = subTopics.map { it.chapterName }.distinct().size
            val orderIdx = subjectOrder.indexOf(subjectName).let { if (it >= 0) it else 99 }
            SubjectEntity(
                subjectId = "${board}_${subjectName.filter { it.isLetterOrDigit() }}",
                board = board,
                subjectName = subjectName,
                totalUnitsCount = unitsCount,
                totalChaptersCount = chaptersCount,
                totalTopicsCount = subTopics.size,
                orderIndex = orderIdx,
                primaryColorHex = colorMap[subjectName] ?: "#6366F1"
            )
        }.sortedBy { it.orderIndex }
    }

    fun getSyllabusUnits(board: String = "BSEB"): List<UnitEntity> {
        val topics = getDefaultSyllabus(board)
        val unitsList = mutableListOf<UnitEntity>()
        var unitCounter = 1

        topics.groupBy { it.subject to it.unitName }.forEach { (key, unitTopics) ->
            val (subject, unitName) = key
            val subjectId = "${board}_${subject.filter { it.isLetterOrDigit() }}"
            val unitId = "${subjectId}_${unitName.filter { it.isLetterOrDigit() }}"
            val chaptersCount = unitTopics.map { it.chapterName }.distinct().size

            // Try parsing unit number from string e.g. "Unit 1: Electrostatics" -> 1
            val unitNum = Regex("""\d+""").find(unitName)?.value?.toIntOrNull() ?: unitCounter

            unitsList.add(
                UnitEntity(
                    unitId = unitId,
                    subjectId = subjectId,
                    board = board,
                    subjectName = subject,
                    unitNumber = unitNum,
                    unitName = unitName,
                    weightageMarks = 8,
                    totalChaptersCount = chaptersCount,
                    totalTopicsCount = unitTopics.size,
                    orderIndex = unitCounter
                )
            )
            unitCounter++
        }
        return unitsList
    }

    fun getSyllabusChapters(board: String = "BSEB"): List<SyllabusChapterEntity> {
        val topics = getDefaultSyllabus(board)
        val chaptersList = mutableListOf<SyllabusChapterEntity>()
        var chapterCounter = 1

        topics.groupBy { Triple(it.subject, it.unitName, it.chapterName) }.forEach { (key, chapTopics) ->
            val (subject, unitName, chapterName) = key
            val subjectId = "${board}_${subject.filter { it.isLetterOrDigit() }}"
            val unitId = "${subjectId}_${unitName.filter { it.isLetterOrDigit() }}"
            val chapterId = "${unitId}_${chapterName.filter { it.isLetterOrDigit() }}"

            val isHighYield = chapTopics.any { it.difficulty.equals("Advanced", ignoreCase = true) }

            chaptersList.add(
                SyllabusChapterEntity(
                    chapterId = chapterId,
                    unitId = unitId,
                    subjectId = subjectId,
                    board = board,
                    subjectName = subject,
                    unitName = unitName,
                    chapterNumber = chapterCounter,
                    chapterName = chapterName,
                    weightageMarks = 8,
                    weightagePercentage = 10f,
                    totalTopicsCount = chapTopics.size,
                    isHighYield = isHighYield,
                    orderIndex = chapterCounter
                )
            )
            chapterCounter++
        }
        return chaptersList
    }
}
