package com.example.data.syllabus

import com.example.data.local.PdfDocumentEntity

object PreloadedPdfsData {
    fun getDefaultDocuments(): List<PdfDocumentEntity> {
        return listOf(
            PdfDocumentEntity(
                id = 1,
                title = "Physics Master Formula & Derivations Sheet",
                category = "FORMULA_SHEET",
                subject = "Physics",
                description = "Complete Class 12 Physics formula book including Electrostatics, Magnetism, Optics & Modern Physics.",
                contentMarkdown = """
# Physics Class 12 — Master Formula Sheet

## 1. Electrostatics
- **Coulomb's Law**: F = (1 / 4πε₀) * (q₁ q₂ / r²) (where 1/4πε₀ = 9 × 10⁹ N m²/C²)
- **Electric Field**: E = F / q = k q / r²
- **Dipole Moment**: p = q × 2a
- **Torque on Dipole**: τ = p × E = p E sin(θ)
- **Gauss's Law**: Φ = ∮ E · dA = q_enclosed / ε₀
  - Infinite straight wire: E = λ / (2πε₀ r)
  - Infinite plane sheet: E = σ / (2ε₀)
- **Capacitance**: C = Q / V
  - Parallel plate capacitor: C = ε₀ A / d (with dielectric: C = K ε₀ A / d)
  - Energy stored: U = (1/2) C V² = Q² / (2C) = (1/2) Q V

## 2. Current Electricity
- **Ohm's Law**: V = I R
- **Drift Velocity**: v_d = (e E τ) / m, Current: I = n e A v_d
- **Resistance**: R = ρ (l / A), Temperature variation: R_T = R_0 (1 + α ΔT)
- **Cell EMF**: V = E - I r (discharging), V = E + I r (charging)
- **Wheatstone Bridge Balance**: P / Q = R / S

## 3. Optics
- **Mirror Formula**: 1/f = 1/v + 1/u
- **Snell's Law**: sin(i) / sin(r) = μ₂ / μ₁
- **Critical Angle**: sin(C) = 1 / μ
- **Lens Maker's Formula**: 1/f = (μ - 1) * (1/R₁ - 1/R₂)
- **Prism Formula**: μ = sin((A + δm)/2) / sin(A/2)
- **YDSE Fringe Width**: β = λ D / d

## 4. Modern Physics
- **Einstein's Photoelectric Equation**: K_max = h ν - φ₀ = e V₀
- **de-Broglie Wavelength**: λ = h / p = h / √(2 m E) = h / √(2 m q V)
- **Radioactive Decay Law**: N = N₀ e^(-λ t), Half-life T₁/₂ = 0.693 / λ
- **Mass-Energy Equivalence**: E = Δm c²
                """.trimIndent(),
                isBookmarked = true,
                lastOpenedTimestamp = System.currentTimeMillis()
            ),
            PdfDocumentEntity(
                id = 2,
                title = "Chemistry Organic Reactions & Named Mechanisms",
                category = "SUMMARY_NOTES",
                subject = "Chemistry",
                description = "All crucial named organic chemistry reactions for Class 12 Boards (Aldol, Cannizzaro, Sandmeyer, etc.)",
                contentMarkdown = """
# Organic Chemistry Named Reactions Compendium

## 1. Haloalkanes & Haloarenes
- **Sandmeyer Reaction**: Benzene diazonium chloride + Cu₂Cl₂/HCl → Chlorobenzene + N₂
- **Finkelstein Reaction**: R-Cl / R-Br + NaI (dry acetone) → R-I + NaCl/NaBr↓
- **Swarts Reaction**: R-Cl / R-Br + AgF / SbF₃ → R-F
- **Wurtz-Fittig Reaction**: Ar-X + 2Na + R-X (dry ether) → Ar-R + 2NaX

## 2. Alcohols, Phenols & Ethers
- **Kolbe's Reaction**: Sodium phenoxide + CO₂ (400 K, 4-7 atm) → H⁺ → Salicylic acid
- **Reimer-Tiemann Reaction**: Phenol + CHCl₃ + 3NaOH → Salicylaldehyde + 3NaCl + 2H₂O
- **Williamson Ether Synthesis**: R'-X + R-O⁻Na⁺ → R-O-R' + NaX (Best with 1° alkyl halide)

## 3. Aldehydes & Ketones
- **Rosenmund Reduction**: Acyl chloride + H₂ (Pd/BaSO₄, quinoline) → Aldehyde
- **Etard Reaction**: Toluene + CrO₂Cl₂ (CS₂) → H₃O⁺ → Benzaldehyde
- **Aldol Condensation**: 2 molecules of aldehyde/ketone having α-hydrogen + dil. NaOH → β-hydroxyaldehyde → Δ → α,β-unsaturated aldehyde.
- **Cannizzaro Reaction**: Aldehydes lacking α-hydrogen (e.g. HCHO, PhCHO) + conc. KOH → Alcohol + Carboxylate salt (Disproportionation).
                """.trimIndent(),
                isBookmarked = true,
                lastOpenedTimestamp = System.currentTimeMillis() - 3600000
            ),
            PdfDocumentEntity(
                id = 3,
                title = "Rudra OS — Class 12 Science High-Yield Mock Paper",
                category = "TEST_PAPER",
                subject = "Physics",
                description = "Standard 70-marks model paper according to the latest BSEB / CBSE exam pattern.",
                contentMarkdown = """
# Class 12 Board Model Paper (Physics)
Time: 3 Hours 15 Minutes | Total Marks: 70

### Section A: Objective / Multiple Choice (1 Mark each)
1. The electric potential on the equatorial plane of an electric dipole is:
   (A) Maximum (B) Minimum (C) Zero (D) Infinite
2. SI unit of self-inductance is:
   (A) Ohm (B) Henry (C) Weber (D) Tesla
3. The optical fiber works on the principle of:
   (A) Total Internal Reflection (B) Scattering (C) Diffraction (D) Polarization

### Section B: Short Answer Questions (2 Marks each)
4. Explain the concept of drift velocity and derive I = n e A v_d.
5. State Brewster's law of polarization.
6. What are eddy currents? How can they be reduced?

### Section C: Long Answer Questions (5 Marks each)
7. State Huygens' principle. Using it, prove the laws of reflection or refraction of light at a plane surface.
8. State Biot-Savart's law. Derive an expression for magnetic field on the axis of a circular current carrying loop.
                """.trimIndent(),
                isBookmarked = false,
                lastOpenedTimestamp = 0L
            )
        )
    }
}
