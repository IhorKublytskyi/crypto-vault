FROM eclipse-temurin:17-jdk-jammy

# 1. Устанавливаем Python 3 и PIP (менеджер пакетов)
RUN apt-get update && apt-get install -y python3 python3-pip && rm -rf /var/lib/apt/lists/*

# 2. Устанавливаем библиотеку для Excel
RUN pip3 install openpyxl

WORKDIR /app

# 3. Копируем JAR файл
COPY target/cryptovault-0.0.1-SNAPSHOT.jar app.jar

# 4. ВАЖНО: Копируем скрипты из исходников в папку внутри контейнера,
# чтобы Python мог их прочитать как обычные файлы.
COPY src/main/resources/scripts/scripts /app/scripts

# Запускаем
ENTRYPOINT ["java","-jar","app.jar"]