# CountryFinder 🇦🇷🌎

[![Code Quality](https://github.com/cspinelli/CountryFinder/actions/workflows/code_quality.yml/badge.svg)](https://github.com/cspinelli/CountryFinder/actions/workflows/code_quality.yml)

CountryFinder es una aplicación Android desarrollada en **Kotlin** con **Jetpack Compose** que permite buscar y explorar información sobre países consumiendo un [Gist](https://gist.githubusercontent.com/hernan-uala/dce8843a8edbe0b0018b32e137bc2b3a/raw/0996accf70cb0ca0e16f9a99e0ee185fafca7af1/cities.json).  

---

## 🚀 Tecnologías y herramientas

- **Kotlin**
- **Jetpack Compose** para la UI
- **MVVM + Clean Architecture** como patrón
- **Retrofit** para consumo del GIST
- **Ktlint** y **Detekt** Como Lint / Análisis estático de código
- **GitHub Actions** para integración continua

---

## 🔎 Problemática del Search

**Contexto:** Usé búsquedas binarias implementando `lowerBound` y `upperBound` para encontrar un rango dentro de la lista de ciudades. Con esto filtré ciudades por prefijo, de manera rápida, con enfoque en la optimización de búsqueda.

**Por qué:** El enunciado comenta que priorice el algoritmo para búsquedas rápidas, entonces decidí invertir tiempo en preprocesar la lista (ordenarla y generar un índice) para que después las búsquedas sean bien rapiditas. Una búsqueda lineal usando filters de `Kotlin` hubiera sido una opción si la lista a procesar no hubiera sido tan grande (aprox 200k ciudades por JSON). Además con este approach podemos reutilizar un índice en memoria sin tener que recalcular la búsqueda a cada rato.

**Preprocesamiento único:**  
   - Al cargar la lista completa de ciudades, se ordena por:
     1. `name` (lowercase, Locale.ROOT)
     2. `country` (lowercase, Locale.ROOT)
   - Con esto podemos hacer búsquedas binarias rápidas (O(log n)) para ubicar las ciudades que coincidan.

**Índice de búsqueda (`CitySearchIndex`):**
   - Guarda la lista ordenada de ciudades y las “keys” (nombres de ciudades en minúsculas).
   - Implementa `lowerBound` y `upperBound` (búsquedas binarias) para encontrar el rango de ciudades que coinciden con el prefijo.
   - Aplica un filtro final para confirmar coincidencia exacta de prefijo (evita falsos positivos).

**Case-scenarios & Unicode-friendly:**
   - Uso de `Locale.ROOT` para normalizar comparaciones y evitar problemas de mayúsculas/minúsculas que dependen del idioma.
   - Incluí un test específico para validar que tanto acentos como caracteres Unicode se incluyan correctamente.

**Performance:**
   - Construcción del índice: O(n log n) solo una vez al cargar la app. Con eso reutilizamos lo que tenemos sin tener que volver a cargar datos
   - Búsqueda: O(log n) para encontrar el rango, O(k) para filtrar resultados.

<img width="512" height="768" alt="searchFlow" src="https://github.com/user-attachments/assets/d6d5a2a4-523e-4160-93d9-ea7edc1ec9a1" />

---

## 🔀 Diagrama de flujo

<img width="512" height="768" alt="appFlow" src="https://github.com/user-attachments/assets/ca8b99ba-0c7d-4559-90ed-4708aaa90f08" />

---

## 🛠 Instalación y ejecución

1. Cloná este repositorio:
   ```bash
   git clone https://github.com/cspinelli/CountryFinder.git
   
2. Dale play ;)

   ---

## 🔨 Próximos Improvements
- Integrar City API para información adicional de las ciudades (bandera, población, etc.).
- Implementar la vista combinada en landscape. No llegué por algunos problemas externos ;(
- Tests de UI más completos. No se enojen pero solo llegué a testear 1 de las 2 activities ;(
- Cacheo offline del listado de ciudades.
- Resolver TODOs: wildcard imports, boilerplate code, etc
- Mejor handleo de errores
- Desacoplar ViewModel, siento que quedó cargadísimo
- Usar recursos para los textos, con traducciones

---
