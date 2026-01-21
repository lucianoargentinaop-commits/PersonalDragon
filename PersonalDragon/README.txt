PersonalDragon (Paper 1.21.x / 1.21.10) - Dragón NO vanilla montable con stamina

IMPORTANTE:
- En este entorno no puedo compilarte el .jar porque no tengo acceso a descargar dependencias de Paper.
- Este proyecto está listo para compilar en tu PC/VPS.

COMPILAR (recomendado):
1) Instala Java 21 (Temurin/Adoptium) y Gradle 8+ (o usa wrapper oficial).
2) En la carpeta del proyecto, ejecuta:
   gradle build
3) El jar sale en:
   build/libs/PersonalDragon-1.1.0.jar

INSTALAR EN TU SERVIDOR:
1) Copia build/libs/PersonalDragon-1.1.0.jar a /plugins/
2) Inicia el servidor para que genere /plugins/PersonalDragon/config.yml
   (ya incluimos config.yml por defecto dentro del jar)
3) En el juego: /pdragon summon

COMANDOS:
- /pdragon summon
- /pdragon despawn
- /pdragon staminareset
