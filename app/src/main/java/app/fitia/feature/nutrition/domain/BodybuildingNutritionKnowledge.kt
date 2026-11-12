package app.fitia.feature.nutrition.domain

/**
 * Referencia local que el chat usa al hablar de comida.
 * Los alimentos son orientativos por 100 g (cocido o crudo, según el nombre).
 */
object BodybuildingNutritionKnowledge {
    fun promptBlock(): String = REFERENCE

    fun forPlanGeneration(): String = PLAN_RANGES

    private val PLAN_RANGES = """
        Rangos de culturismo para ajustar el plan (adáptalos al peso y al objetivo, no los copies a ciegas):
        - Proteína: 1,6–2,2 g/kg en mantenimiento o volumen; 2,0–2,6 g/kg en definición. La app parte de 2,0 g/kg.
        - Grasa: 0,6–1,0 g/kg. No bajar de ~0,5 g/kg ni del 20 % de las kcal. La app parte de 0,8 g/kg.
        - Carbohidratos: el resto de las kcal. Más altos en días de entreno.
        - Volumen: mantenimiento +200 a +350 kcal (la app suma +300). Ritmo ~0,25–0,5 % del peso corporal por semana.
        - Definición: mantenimiento −300 a −500 kcal (la app resta 500). Ritmo ~0,5–1 % del peso por semana.
        - Agua: ~35 ml/kg, más 500–1000 ml por hora de entreno duro. La app usa 35 ml/kg.
        - Energía de la app: Mifflin-St Jeor × actividad (sedentario 1,2; ligero 1,375; moderado 1,55; activo 1,725; muy activo 1,9).
    """.trimIndent()

    private val REFERENCE = """
        ## Referencia de nutrición y alimentación de culturismo
        Úsala cuando el usuario pregunte por comida, macros, definición, volumen, suplementos o cómo comer.
        Los números de alimentos son orientativos por 100 g. Para anotar en el diario, usa esta tabla o los alimentos guardados del usuario; no inventes kcal.
        Adapta siempre los rangos al peso, sexo, objetivo y objetivos reales de la app.
        Esto es orientación de entreno y comida, no un diagnóstico. Si hay embarazo, un trastorno de la conducta alimentaria, una enfermedad o medicación, que lo lleve un médico. No recomiendes diuréticos, deshidratación, laxantes ni dietas de hambre.

        ### Principios
        - El peso cambia sobre todo por el balance de energía. El músculo se gana o se conserva con entrenamiento de fuerza y proteína suficiente.
        - Prioridad: calorías del objetivo → proteína → grasa mínima → carbohidratos con el resto → comida que se pueda sostener → sueño y constancia.
        - La báscula sube y baja con glucógeno, creatina, sodio, fibra y ciclo menstrual. Mira la media semanal, no un día suelto.
        - Comida real la mayor parte del tiempo (carne, pescado, huevos, lácteos, legumbres, patata, arroz, avena, fruta, verdura, aceite de oliva). Lo ultraprocesado cabe si los macros cuadran, pero no como base.
        - Alcohol: ~7 kcal/g, peor recuperación y peor control del déficit. En definición, lo mínimo.
        - Fibra: 25–40 g/día. Alrededor del entreno, menos fibra y menos grasa si se quiere digestión rápida.
        - Sal: no hace falta restringirla si se suda y se come comida real. Sube el peso de agua, no la grasa.

        ### Energía y ritmo
        - Mantenimiento de la app: Mifflin-St Jeor × factor de actividad. Es un punto de partida; el peso real de las semanas siguientes manda.
        - Volumen (ganar músculo limitando grasa): +200 a +350 kcal sobre mantenimiento, o +10 %. Intermedios y avanzados: hacia el lado bajo. Principiantes pueden ir un poco más arriba.
        - Definición: −300 a −500 kcal, o −10 a −20 %. Déficits mayores solo poco tiempo y no por debajo de ~1200 kcal sin supervisión.
        - Ritmo deseable: volumen +0,25–0,5 % del peso/semana (alguien de 80 kg: ~0,2–0,4 kg/semana). Definición −0,5–1 %/semana (80 kg: ~0,4–0,8 kg/semana). Si la cintura sube rápido en volumen, baja el superávit. Si en definición caes fuerza y energía de golpe, suaviza el déficit.
        - Recomposición (perder grasa y ganar músculo a la vez): más realista en principiantes, gente que vuelve a entrenar o con más grasa corporal. Calorías en mantenimiento o déficit suave, proteína alta, fuerza progresiva. Quien ya está muy definido y avanzado gana poco músculo en déficit.
        - Cada 8–12 semanas de definición exigente, 7–14 días en mantenimiento ayudan. Un día más alto en hidratos no es un atracón: sube carbohidratos, mantén la proteína y no dispares la grasa.

        ### Proteína
        - Mantenimiento y volumen: 1,6–2,2 g/kg de peso.
        - Definición, sobre todo si ya se está definido: 2,0–2,6 g/kg. Por encima de ~2,2 g/kg la ganancia extra es pequeña; sirve sobre todo para saciedad y para no perder músculo.
        - Reparte en 3–5 tomas. Cada toma, ~0,3–0,4 g/kg (en la práctica 25–50 g) con unos 2–3 g de leucina: carne, pescado, huevos, lácteos o whey lo cubren.
        - El “timing” exacto importa menos que el total del día. Una toma de proteína en las horas de alrededor del entreno es suficiente.
        - Fuentes completas: pollo, pavo, ternera, cerdo magro, pescado, marisco, huevo, lácteos, whey, caseína. Vegetales: tofu, seitán, soja texturizada, legumbres + cereal a lo largo del día. Quien no come animal necesita B12.
        - La clara sola es proteína magra; el huevo entero aporta grasa y micronutrientes. Mezclar ambos es lo habitual.

        ### Grasas
        - 0,6–1,0 g/kg. Suelo útil: no bajar de 0,5 g/kg ni de ~20 % de las kcal, por hormonas, saciedad y absorción de vitaminas.
        - Reparte: aceite de oliva, aguacate, frutos secos, pescado azul, yema. La saturada (queso curado, carne grasa, mantequilla) puede estar, sin ser la grasa principal.
        - Omega-3: pescado azul 2 veces por semana o, si no se come, 1–2 g/día de EPA+DHA.
        - En días de descanso se puede subir un poco la grasa y bajar hidratos, con las mismas calorías. No es obligatorio.

        ### Carbohidratos
        - Son el resto: kcal − (proteína × 4) − (grasa × 9), dividido entre 4.
        - Priorízalos en la comida previa y posterior al entreno: arroz, patata, boniato, pasta, avena, fruta, pan, tortitas de arroz.
        - Preentreno (1–3 h antes, si sienta bien): 0,5–1 g/kg de hidrato, proteína moderada, poca grasa y poca fibra.
        - Sesiones de más de ~90 min: algo de hidrato durante puede ayudar. En un entreno normal de gimnasio no hace falta.
        - Postentreno: 0,5–1 g/kg junto con proteína. No hay una ventana de 30 minutos mágica; cuenta la comida de las horas siguientes.
        - Verdura en casi todas las comidas. Fruta 1–3 piezas. Azúcar y miel caben alrededor del entreno o para llegar a los hidratos, no como base.

        ### Agua, sodio y micros
        - Agua: ~35 ml/kg. En entreno duro, +500–1000 ml por hora según el sudor. La orina útil es amarillo claro, no transparente a todas horas.
        - Creatina monohidrato retiene agua dentro del músculo: la báscula sube 0,5–1,5 kg. No es grasa.
        - A vigilar si la dieta es muy restrictiva o poco variada: hierro (sobre todo mujeres), vitamina D, calcio, magnesio, zinc, yodo, B12, potasio (patata, fruta, legumbres, verdura).
        - No hace falta un multivitamínico si se come variado. Sí tiene sentido vitamina D si hay poco sol, y omega-3 si no hay pescado.

        ### Definición
        - Mantén el entrenamiento pesado. Se puede bajar un poco el volumen de series, no convertir el gym en solo cardio.
        - Proteína alta, déficit moderado, verdura y saciedad. Cardio: pasos diarios primero (8–12 mil) y algo de cardio suave si el déficit por comida ya es grande.
        - Si te estancas 2 semanas (media de peso y medidas), quita ~150–200 kcal o añade pasos. No cortes de golpe.
        - Últimos kilos: ve más lento. Manipular agua, sodio y carbohidratos la semana del escenario es individual y fácil de hacer mal; no des un protocolo de pico ni de deshidratación.
        - Señales de que el déficit es excesivo: frío constante, sueño roto, pérdida clara de fuerza, en mujeres pérdida de regla, obsesión con la comida. Sube calorías hacia mantenimiento.

        ### Volumen
        - Superávit pequeño. El objetivo es subir fuerza y una media de peso lenta, no “comer todo”.
        - Comidas densas si cuesta comer: aceite de oliva, frutos secos, arroz, lácteos enteros, un batido (leche, avena, fruta, whey). Los gainers comerciales sobran si se puede comer comida.
        - Si en 2 semanas no sube la media de peso, suma ~150 kcal, casi todas de hidratos.
        - Proteína se mantiene. No hace falta “ensuciar” la dieta para crecer.

        ### Suplementos (lo que de verdad importa)
        - Creatina monohidrato: 3–5 g al día, cualquier hora, sin necesidad de cargar. Es el suplemento con mejor respaldo para fuerza y músculo. Bebe agua. Si hay enfermedad renal, que lo consulte un médico.
        - Cafeína: 3–6 mg/kg antes de entrenar si se tolera. No tapa un mal sueño.
        - Proteína en polvo: es comida cómoda (whey, caseína o vegetal). No es obligatoria si la proteína de la dieta ya llega.
        - Vitamina D y omega-3: solo si la dieta o el sol no cubren.
        - Beta-alanina y citrulina: efecto pequeño en algunas sesiones. Opcionales.
        - Lo que no hace falta recomendar: quemagrasas, “boosters” de testosterona, BCAA si la proteína ya es alta, productos milagro, insulina, hormonas o fármacos. Si preguntan por esteroides u otros anabolizantes, no des pautas de dosis ni ciclos; explica riesgos y que eso es decisión médica, no un consejo de la app.

        ### Día tipo (ajusta gramos al objetivo real)
        - Desayuno: avena + lácteo o whey + fruta; o huevos + pan o patata + fruta.
        - Comida: arroz o patata + pollo, pavo, ternera o tofu + verdura + aceite de oliva.
        - Pre o merienda: yogur griego o queso batido + fruta, o atún + tortitas de arroz.
        - Cena: pescado, carne o huevos + patata o arroz + ensalada.
        - Si faltan kcal en volumen: un puñado de frutos secos o un vaso de leche con avena.
        - Si sobran kcal en definición: más verdura, lácteo 0 %, claras y menos aceite y frutos secos.

        ### Errores frecuentes
        - Volumen sucio “para crecer” y luego meses de definición.
        - Definición con proteína baja y cardio excesivo: se pierde fuerza.
        - Cambiar la dieta cada tres días por un peso diario.
        - Creer que un alimento “engorda” fuera de las calorías de la semana.
        - Quitar la grasa a cero o quitar los hidratos a cero.
        - No contar el aceite, los frutos secos, los refrescos y el alcohol.
        - Copiar la dieta de un culturista profesional, que suele implicar fármacos y no es el caso de esta app.

        ### Alimentos por 100 g (kcal, proteína, carbohidratos, grasa)
        Proteína animal:
        - Pechuga de pollo cruda: 120 kcal, 22,5 P, 0 C, 2,6 G. Cocida: 165 kcal, 31 P, 0 C, 3,6 G.
        - Muslo de pollo sin piel cocido: 180 kcal, 25 P, 0 C, 8 G.
        - Pechuga de pavo cocida: 135 kcal, 30 P, 0 C, 1 G.
        - Ternera magra cocida (redondo o solomillo): 180 kcal, 28 P, 0 C, 7 G.
        - Carne picada de vacuno 5 % cruda: 137 kcal, 21 P, 0 C, 5 G. 10 % cruda: 176 kcal, 20 P, 0 C, 10 G.
        - Lomo de cerdo cocido: 145 kcal, 27 P, 0 C, 4 G.
        - Jamón cocido extra: 110 kcal, 19 P, 1 C, 3 G.
        - Atún al natural escurrido: 116 kcal, 26 P, 0 C, 1 G.
        - Salmón cocido: 208 kcal, 20 P, 0 C, 13 G.
        - Caballa cocida: 205 kcal, 19 P, 0 C, 14 G.
        - Sardinas escurridas: 208 kcal, 25 P, 0 C, 11 G.
        - Merluza cocida: 90 kcal, 18 P, 0 C, 1,5 G.
        - Bacalao cocido: 105 kcal, 23 P, 0 C, 1 G.
        - Gambas cocidas: 99 kcal, 24 P, 0 C, 0,3 G.
        - Huevo entero: 143 kcal, 13 P, 1 C, 10 G.
        - Clara de huevo: 52 kcal, 11 P, 0,7 C, 0,2 G.
        Lácteos y polvos:
        - Leche semidesnatada: 46 kcal, 3,4 P, 4,8 C, 1,6 G.
        - Leche desnatada: 35 kcal, 3,4 P, 5 C, 0,2 G.
        - Yogur griego 0 %: 57 kcal, 10 P, 4 C, 0,2 G.
        - Skyr natural: 63 kcal, 11 P, 4 C, 0,2 G.
        - Queso fresco batido 0 %: 70 kcal, 12 P, 4 C, 0,3 G.
        - Requesón desnatado: 80 kcal, 12 P, 4 C, 1 G.
        - Mozzarella light: 170 kcal, 19 P, 2 C, 10 G.
        - Queso semicurado: 370 kcal, 25 P, 1 C, 29 G.
        - Whey isolate en polvo: 370 kcal, 85 P, 3 C, 1 G.
        - Caseína en polvo: 370 kcal, 80 P, 5 C, 2 G.
        Vegetal proteico:
        - Tofu firme: 145 kcal, 17 P, 2 C, 9 G.
        - Seitán cocido: 120 kcal, 24 P, 5 C, 1,5 G.
        - Soja texturizada seca: 330 kcal, 50 P, 30 C, 1,5 G. Hidratada pesa mucho más y baja la densidad.
        - Edamame cocido: 121 kcal, 12 P, 9 C, 5 G.
        - Lentejas cocidas: 116 kcal, 9 P, 20 C, 0,4 G.
        - Garbanzos cocidos: 164 kcal, 9 P, 27 C, 2,6 G.
        - Alubias cocidas: 127 kcal, 9 P, 23 C, 0,5 G.
        Hidratos:
        - Arroz blanco cocido: 130 kcal, 2,7 P, 28 C, 0,3 G.
        - Arroz integral cocido: 123 kcal, 2,7 P, 26 C, 1 G.
        - Avena cruda: 389 kcal, 17 P, 66 C, 7 G.
        - Pasta cocida: 131 kcal, 5 P, 25 C, 1 G.
        - Patata cocida: 87 kcal, 2 P, 20 C, 0,1 G.
        - Boniato cocido: 90 kcal, 2 P, 21 C, 0,2 G.
        - Quinoa cocida: 120 kcal, 4,4 P, 21 C, 1,9 G.
        - Cuscús cocido: 112 kcal, 4 P, 23 C, 0,2 G.
        - Pan integral: 250 kcal, 13 P, 41 C, 4 G.
        - Pan de molde blanco: 265 kcal, 9 P, 49 C, 3 G.
        - Tortitas de arroz: 387 kcal, 8 P, 81 C, 3 G.
        - Plátano: 89 kcal, 1 P, 23 C, 0,3 G.
        - Manzana: 52 kcal, 0,3 P, 14 C, 0,2 G.
        - Naranja: 47 kcal, 0,9 P, 12 C, 0,1 G.
        - Fresas: 32 kcal, 0,7 P, 8 C, 0,3 G.
        - Arándanos: 57 kcal, 0,7 P, 14 C, 0,3 G.
        - Miel: 304 kcal, 0,3 P, 82 C, 0 G.
        - Dátiles: 280 kcal, 2 P, 75 C, 0,4 G.
        Grasas:
        - Aceite de oliva: 884 kcal, 0 P, 0 C, 100 G. Una cucharada (~10 g) son ~90 kcal.
        - Aguacate: 160 kcal, 2 P, 9 C, 15 G.
        - Almendras: 579 kcal, 21 P, 22 C, 50 G.
        - Crema de cacahuete: 588 kcal, 25 P, 20 C, 50 G.
        - Nueces: 654 kcal, 15 P, 14 C, 65 G.
        - Semillas de chía: 486 kcal, 17 P, 42 C, 31 G.
        - Chocolate negro 85 %: 580 kcal, 11 P, 22 C, 46 G.
        Verdura (poca energía, úsala para volumen del plato):
        - Brócoli cocido: 35 kcal, 2,4 P, 7 C, 0,4 G.
        - Espinacas: 23 kcal, 3 P, 3,6 C, 0,4 G.
        - Calabacín: 17 kcal, 1,2 P, 3 C, 0,3 G.
        - Tomate: 18 kcal, 0,9 P, 4 C, 0,2 G.
        - Lechuga: 15 kcal, 1,4 P, 3 C, 0,2 G.
        - Pimiento: 31 kcal, 1 P, 6 C, 0,3 G.
        - Champiñón: 22 kcal, 3 P, 3 C, 0,3 G.
        - Zanahoria: 41 kcal, 0,9 P, 10 C, 0,2 G.

        ### Cómo pasar la tabla a un plato
        - Multiplica por los gramos y divide entre 100. Ejemplo: 180 g de pechuga cocida = 1,8 × (165 kcal, 31 P) ≈ 297 kcal y 56 g de proteína. 250 g de arroz cocido ≈ 325 kcal y 70 g de hidratos. 10 g de aceite de oliva ≈ 90 kcal.
        - Cuando propongas una comida, da gramos y el total de kcal y macros de ese plato.
        - Si el usuario ya tiene el alimento en la biblioteca, manda esos números, no los de esta tabla.
    """.trimIndent()
}
