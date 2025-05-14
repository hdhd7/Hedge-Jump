import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PFont;

import ddf.minim.*;

import java.util.ArrayList;
import java.util.Random;

public class Main extends PApplet {

    float bgX, bgY;
    final int PLATFORM_BUFFER = 50; // Prevent platforms from appearing within 50px of the top

    public static void main(String[] args) {
        PApplet.main("Main");
    }
    String gameState = "menu";
    Button startButton, settingsButton, exitButton, menuButton;
    final int WIDTH = 400;
    final int HEIGHT = 650;
    Doodler doodler;
    ArrayList<Platform> platforms;
    Random random;
    boolean isGameOver = false;
    int countDown = 3;
    boolean isCountdownFinished = false;
    long goTime = 1000;
    long startTime;

    public void settings() {
        size(WIDTH, HEIGHT);
    }

    PImage bgImage;
    PImage playerImg;
    PImage platformImg;
    PFont customFont;
    PImage monsterImg;
    PImage menuBgImage;
    PImage countdownGameOverBgImage;
    PImage playerJumpImg;
    PImage pointsImage;
    PImage bulletImage;

    Slider volumeSlider;

    Minim minim;
    AudioPlayer player;
    AudioPlayer gameOverSound;
    AudioPlayer jumpSound;
    AudioPlayer scoreMilestoneSound;
    AudioPlayer resetSound;
    AudioPlayer monsterKillSound;
    AudioPlayer buttonHoverSound;
    AudioPlayer buttonClickSound;
    AudioPlayer menuReturnSound;


    public void setup() {
        bgX = 0;
        bgY = 0;
        surface.setTitle("Hedge Jump");
        surface.setIcon(loadImage("shadowjump.png"));

        customFont = createFont("pixelcyr_normal.ttf", 16);
        textFont(customFont);

        minim = new Minim(this);
        player = minim.loadFile("friday.mp3");
        gameOverSound = minim.loadFile("gameover.mp3");
        jumpSound = minim.loadFile("jump.mp3");

        scoreMilestoneSound = minim.loadFile("okay.mp3");
        scoreMilestoneSound.setGain(-20);

        resetSound = minim.loadFile("BTN3.wav");
        resetSound.setGain(-15);
        resetSound.rewind();

        buttonHoverSound = minim.loadFile("BTN0.wav");
        buttonHoverSound.setGain(-15);
        buttonClickSound = minim.loadFile("BTN1.wav");
        buttonClickSound.setGain(-15);

        menuReturnSound = minim.loadFile("BTN2.wav");
        menuReturnSound.setGain(-15);

        monsterKillSound = minim.loadFile("BONUS.wav");
        monsterKillSound.setGain(-15);
        startButton = new Button(WIDTH / 2, HEIGHT / 3, "Начать");
        settingsButton = new Button(WIDTH / 2, HEIGHT / 2, "Настройки");
        exitButton = new Button(WIDTH / 2, HEIGHT / 3 * 2, "Выйти");
        volumeSlider = new Slider(WIDTH / 2, HEIGHT / 2, 200, 30, 0, 100, 50);
        menuButton = new Button(WIDTH / 2, HEIGHT / 3 * 2, "Меню");
        gameState = "menu";


        player.loop();
        player.setGain(-30);

        gameOverSound.setGain(-20);

        rectMode(CENTER);
        doodler = new Doodler(WIDTH / 2, HEIGHT - 70);
        platforms = new ArrayList<>();
        platforms.add(new Platform(WIDTH / 2, HEIGHT - 30));
        platforms.add(new Platform(WIDTH / 3 * 2, HEIGHT / 5 * 1));
        platforms.add(new Platform(WIDTH / 4 * 1, HEIGHT / 5 * 2));
        platforms.add(new Platform(WIDTH / 3 * 1, HEIGHT / 5 * 3));
        platforms.add(new Platform(WIDTH / 4 * 3, HEIGHT / 5 * 4));

        for (Platform platform : platforms) {
            if (random(0, 1) < 0.05) {
                platform.hasMonster = true;
            }
        }

        platforms.get(0).hasMonster = false;
        platforms.get(0).isMoving = false;
        platforms.get(1).hasMonster = false;
        platforms.get(1).isMoving = false;


        for (Platform platform : platforms) {
            if (random(0, 1) < 0.07) {
                platform.isMoving = true;
            }
        }

        isCountdownFinished = false;
        gameState = "menu";

        bgImage = loadImage("background_400x640.png");
        playerImg = loadImage("player1.png");
        platformImg = loadImage("cloudplatform.png");
        menuBgImage = loadImage("background_menu.png");
        countdownGameOverBgImage = loadImage("background_gameover.png");
        playerJumpImg = loadImage("player_jump.png");

        bulletImage = loadImage("fireball.png");
        bulletImage.resize(20, 20);


        monsterImg = loadImage("monster.png");
        pointsImage = loadImage("plus50points.png");
        monsterImg.resize(40, 40);

        bgImage.resize(WIDTH, HEIGHT);
    }

    public void mouseMoved() {
        if (gameState.equals("menu")) {
            if (startButton.isHovered(mouseX, mouseY) || settingsButton.isHovered(mouseX, mouseY) || exitButton.isHovered(mouseX, mouseY)) {
                if (!buttonHoverSound.isPlaying()) {
                    buttonHoverSound.rewind();
                    buttonHoverSound.play();
                }
            }
        }
    }

    public void mousePressed() {
        if (gameState.equals("menu")) {
            if (startButton.isClicked(mouseX, mouseY) || settingsButton.isClicked(mouseX, mouseY) || exitButton.isClicked(mouseX, mouseY)) {
                buttonClickSound.rewind();
                buttonClickSound.play();
            }
            if (startButton.isClicked(mouseX, mouseY)) {
                gameState = "game";
                isCountdownFinished = false; // Start the countdown before the game starts
                startTime = millis(); // Reset the start time for the countdown
            } else if (settingsButton.isClicked(mouseX, mouseY)) {
                gameState = "settings";
            } else if (exitButton.isClicked(mouseX, mouseY)) {
                exit();
            }
        } else if (gameState.equals("settings")) {
            if (menuButton.isClicked(mouseX, mouseY)) {
                gameState = "menu";
                // Воспроизводим звук при возвращении в меню
                menuReturnSound.rewind();
                menuReturnSound.play();
            } else if (volumeSlider.isClicked(mouseX, mouseY)) {
                volumeSlider.updateValue(mouseX);
                player.setGain(map(volumeSlider.value, 0, 100, -80, 0)); // Update the volume of the music
            }
        }
    }

    void resetGame() {
        if (gameOverSound.isPlaying()) {
            gameOverSound.pause();
            gameOverSound.rewind();
        }
        resetSound.rewind(); // Rewind to the beginning of the sound
        resetSound.play(); // Play the reset sound

        doodler = new Doodler(WIDTH / 2, HEIGHT - 70);
        doodler.score = 0; // Add this line to reset the score
        platforms.clear(); // Clear any existing platforms
        platforms.add(new Platform(WIDTH / 2, HEIGHT - 30));
        platforms.add(new Platform(WIDTH / 3 * 2, HEIGHT / 5 * 1));
        platforms.add(new Platform(WIDTH / 4 * 1, HEIGHT / 5 * 2));
        platforms.add(new Platform(WIDTH / 3 * 1, HEIGHT / 5 * 3));
        platforms.add(new Platform(WIDTH / 4 * 3, HEIGHT / 5 * 4));

        // Устанавливаем hasMonster и isMoving в false для первых двух платформ
        platforms.get(0).hasMonster = false;
        platforms.get(0).isMoving = false;
        platforms.get(1).hasMonster = false;
        platforms.get(1).isMoving = false;

        isGameOver = false;
        isCountdownFinished = false; // Сбросить признак того, что обратный отсчет завершился
        startTime = millis(); // Сбросить время для обратного отсчета
        loop(); // This should start the draw loop again, if it's been stopped

        gameOverSound.rewind(); // Перематываем звук на начало
    }

    public void mouseDragged() {
        if (gameState.equals("settings")) {
            if (volumeSlider.isClicked(mouseX, mouseY)) {
                volumeSlider.updateValue(mouseX);
                player.setGain(map(volumeSlider.value, 0, 100, -80, 0)); // Update the volume of the music
            }
        }
    }

    public void regeneratePlatforms() {
        float minY = platforms.get(0).y;
        for (Platform platform : platforms) {
            if (platform.y < minY) {
                minY = platform.y;
            }
        }

        // Определите высоту прыжка вашего персонажа здесь (например, 150 пикселей)
        float doodlerJumpHeight = 150;

        // Установите диапазон по горизонтали, внутри которого платформы могут спауниться
        float rangeX = WIDTH - PLATFORM_BUFFER * 2; // Учитываем буфер для краев экрана
        float startX = PLATFORM_BUFFER;

        // Расстояние между платформами по вертикали
        float verticalSpacing = doodlerJumpHeight; // Убираем `- Platform.h`, т.к. h не является статическим свойством

        for (int i = 0; i < platforms.size(); i++) {
            Platform platform = platforms.get(i);
            if (platform.y > HEIGHT) {
                // Если платформа вышла за пределы экрана, создайте новую платформу.
                float newX = random(startX, startX + rangeX);
                float newY = minY - verticalSpacing;
                Platform newPlatform = new Platform(newX, newY);
                platforms.set(i, newPlatform); // Замените старую платформу новой

                if (random(0, 1) < 0.07) {  // Устанавливаем вероятность движения платформы
                    newPlatform.isMoving = true;
                }

                if (random(0, 1) < 0.05) {  // Устанавливаем вероятность спавна монстра на платформе
                    newPlatform.hasMonster = true;
                }
            }
        }
    }


    public void draw() {
        if (gameState.equals("menu")) {
            background(menuBgImage);
            fill(255); // White color for text
            textAlign(CENTER, CENTER); // Center alignment for text

            textSize(50); // Large text size for title
            text("Hedge Jump", WIDTH / 2, HEIGHT / 5); // Display title in the top center

            textSize(18); // Normal text size for buttons
            startButton.show();
            settingsButton.show();
            exitButton.show();

            textSize(12); // Small text size for credits
            fill(0);
            text("Made by hdhd7 aka. hxlyyyrgz and dany444", WIDTH / 2, HEIGHT - HEIGHT / 10); // Display credits in the bottom center
        } else if (gameState.equals("settings")) {
            background(menuBgImage); // Yellow background
            volumeSlider.show();
            menuButton.show(); // Add this line

            fill(0); // Black text
            textAlign(CENTER, CENTER);
            textSize(20); // Уменьшаем размер шрифта
            text("Управление:", WIDTH / 2, HEIGHT / 4); // Смещаем текст вверх
            text("Стрелки влево/вправо - движение", WIDTH / 2, HEIGHT / 4 + 30);
            text("Стрелка вверх - стрельба", WIDTH / 2, HEIGHT / 4 + 60);
        } else if (gameState.equals("game")) {
            if (!isCountdownFinished) {
                background(countdownGameOverBgImage);  // Черный фон
                float elapsed = millis() - startTime;
                if (elapsed < countDown * 1000) {
                    // Текст «3», «2», «1» по центру экрана
                    int count = countDown - (int) (elapsed / 1000);  // Преобразуем миллисекунды в секунды и вычитаем из обратного отсчета
                    fill(255);  // Белый цвет цифр
                    textSize(50);
                    textAlign(CENTER, CENTER);  // Выравнивание текста по центру
                    text(count, width / 2, height / 2);  // Отображаем цифры по центру

                } else if (!isCountdownFinished) {

                    isCountdownFinished = true;
                    startTime = millis();  // Сброс времени для начала игры
                }

                return; // Не выполнять остальной код draw, пока идет обратный отсчет
            }

            background(bgImage);

            for (Platform platform : platforms) {
                if (doodler.lands(platform)) {
                    doodler.jump();
                    break;
                }

                if (platform.hasMonster && doodler.hitsMonster(platform)) {
                    isGameOver = true;
                    if (!gameOverSound.isPlaying()) {
                        gameOverSound.rewind();
                        gameOverSound.play();
                    }
                }
            }

            doodler.move();
            doodler.updateBullets();
            if (doodler.y < HEIGHT / 2 && doodler.dy < 0) {
                float shift = doodler.dy;
                for (Platform platform : platforms) {
                    platform.y -= shift;
                }
                doodler.y -= shift; // Оставляйте персонажа в центре экрана
                doodler.highestY -= shift; // Adjust highestY to reflect the downward movement of platforms
                bgY -= shift; // Обновляем смещение фона
                bgY %= bgImage.height; // Убедитесь, что bgY остаётся в пределах высоты фона
                regeneratePlatforms();
            }

            doodler.show();
            for (Platform platform : platforms) {
                platform.show();
            }

            if (isGameOver) {
                background(0);  // Чёрный фон для экрана GAME OVER
                fill(255);  // Белый цвет для текста
                textSize(50);  // Размер шрифта для "GAME OVER"
                textAlign(CENTER, CENTER);  // Выравнивание текста по центру
                textFont(customFont, 50); // Установите шрифт и размер шрифта перед отображением текста
                text("GAME OVER", width / 2, height / 3);  // Позиционируем "GAME OVER" ближе к верхней части экрана

                textSize(25);  // Установите подходящий размер шрифта
                textFont(customFont, 25);
                text("Нажми R для перезапуска", width / 2, height / 3 + 50);  // Расположите текст чуть ниже "GAME OVER"

                textSize(30); // Меньший размер шрифта для отображения счета
                text("Score: " + doodler.score, width / 2, height / 3 + 90); // Отображение счета ниже надписи "GAME OVER"
                textFont(customFont, 30); // Установите шрифт и размер шрифта перед отображением текста

                textSize(25);  // Установите подходящий размер шрифта
                textFont(customFont, 25);
                text("ESC для выхода в меню", width / 2, height / 3 + 150);  // Расположите текст чуть ниже "GAME OVER"

                noLoop(); // Останавливаем цикл draw
                return; // Выходим из функции, чтобы не выполнять оставшуюся часть кода

            } else {
                if (doodler.isBlinking) {
                    doodler.updateBlinkState();
                } else {
                    doodler.drawScore(color(255, 255, 255));
                }

                textSize(20); // Set text size for score
                textAlign(LEFT, TOP); // Set text alignment for score
                textFont(customFont, 20); // Set font for score
                text("Счёт: " + doodler.score, 10, 10); // Display the score
            }
        }
        else if (gameState.equals("settings")) {
            background(menuBgImage); // Yellow background
            volumeSlider.show();
            menuButton.show(); // Add this line

            fill(0); // Black text
            textAlign(CENTER, CENTER);
            textSize(20); // Уменьшаем размер шрифта
            text("Управление:", WIDTH / 2, HEIGHT / 4); // Смещаем текст вверх
            text("Стрелки влево/вправо - движение", WIDTH / 2, HEIGHT / 4 + 30);
            text("Стрелка вверх - стрельба", WIDTH / 2, HEIGHT / 4 + 60);
        }
    }

    public void stop() {
        // Останавливаем и освобождаем ресурсы музыкального проигрывателя
        player.close();
        gameOverSound.close();
        jumpSound.close(); // Освобождаем jumpSound
        scoreMilestoneSound.close();
        resetSound.close(); // Release the reset sound
        monsterKillSound.close();

        minim.stop();

        super.stop();
    }


    public void keyPressed() {
        if (key == ESC) {
            key = 0;  // Disable the standard Escape key behavior
            if (isGameOver) {  // If the game is over
                resetGame();
                if (resetSound.isPlaying()) {
                    resetSound.pause();
                    resetSound.rewind();
                }
                gameState = "menu";  // Switch to the menu
                loop();  // Restart the draw loop
                menuReturnSound.rewind();
                menuReturnSound.play(); // Play the menu return sound
            }
        }
        if (isGameOver) {
            // Ensure that only the 'R' key can reset the game, and only if the game is over
            if ((key == 'r' || key == 'R' || key == 'к' || key == 'К') && isGameOver) {
                resetGame();
            }
        } else {
            // Check if LEFT or RIGHT has been pressed if the game is not over
            if (key == CODED) { // Check if the keystroke represents a special key
                if (keyCode == LEFT) {
                    doodler.dx = -4;
                } else if (keyCode == RIGHT) {
                    doodler.dx = 4;
                }else if (keyCode == UP) {
                    doodler.shoot();
                }
            }
        }
    }


    public void keyReleased() {
        if (key == CODED) { // Check if the released key is a special key
            if (keyCode == LEFT || keyCode == RIGHT) {
                doodler.dx = 0;
            }
        }
    }
    public class Button {
        float x, y;
        String text;
        float w = 100, h = 50;
        boolean isHoveredOver = false; // Add this line


        Button(float x, float y, String text) {
            this.x = x;
            this.y = y;
            this.text = text;
        }

        boolean isHovered(float mx, float my) {
            boolean currentlyHovered = (mx > x - w / 2 && mx < x + w / 2 && my > y - h / 2 && my < y + h / 2);
            if (currentlyHovered) {
                if (!isHoveredOver) {
                    isHoveredOver = true;
                    return true;
                }
            } else {
                isHoveredOver = false;
            }
            return false;
        }

        void show() {
            fill(0); // Black color for button
            rectMode(CENTER);
            rect(x, y, w, h);
            fill(255); // White color for text
            textAlign(CENTER, CENTER);
            text(text, x, y);
        }

        boolean isClicked(float mx, float my) {
            return (mx > x - w / 2 && mx < x + w / 2 && my > y - h / 2 && my < y + h / 2);
        }
    }

    public class Slider {
        float x, y, w, h, min, max, value;

        Slider(float x, float y, float w, float h, float min, float max, float value) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.min = min;
            this.max = max;
            this.value = value;
        }

        void show() {
            fill(255); // White color for the slider
            rectMode(CENTER);
            rect(x, y, w, h);

            float knobX = map(value, min, max, x - w / 2, x + w / 2); // Calculate the x position of the knob
            fill(0); // Black color for the knob
            rect(knobX, y, h, h);

            fill(0); // Black text
            textAlign(CENTER, CENTER);
            text("Volume: " + (int)value, x, y - 30); // Display the volume above the slider
        }

        boolean isClicked(float mx, float my) {
            return (mx > x - w / 2 && mx < x + w / 2 && my > y - h / 2 && my < y + h / 2);
        }

        void updateValue(float mx) {
            value = map(mx, x - w / 2, x + w / 2, min, max); // Calculate the new value based on the mouse x position
            value = constrain(value, min, max); // Make sure the value doesn't go beyond min or max
        }
    }

    public class Bullet {
        float x, y, speed;
        Bullet(float x, float y) {
            this.x = x;
            this.y = y;
            this.speed = 10;
        }

        void update() {
            y -= speed;
        }

        void show() {
            imageMode(CENTER);
            image(bulletImage, x, y);
        }

        boolean hitsMonster(Platform platform) {
            if (platform.hasMonster) {
                float monsterX = platform.x;
                float monsterY = platform.y - platform.h / 2 - 10;
                float monsterSize = 40;
                return (x > monsterX - monsterSize / 2 && x < monsterX + monsterSize / 2 &&
                        y > monsterY - monsterSize / 2 && y < monsterY + monsterSize / 2);
            } else {
                return false;
            }
        }
    }
    public class Doodler {
        float x, y, w, h, dy, dx;
        int score;
        static int highScore = 0;
        float highestY; // Add this variable to track the highest point reached
        Platform lastPlatformLanded = null;

        int lastMilestoneScore = 0; // Добавьте эту переменную в класс Doodler

        boolean isBlinking = false;
        int blinkState = 0;
        long blinkDuration = 200; // Duration of each blink, example 200 milliseconds
        int blinkCount = 3; // Total number of blinks
        long blinkStartTime;
        ArrayList<Bullet> bullets;

        boolean hitsMonster(Platform platform) {
            float monsterX = platform.x;
            float monsterY = platform.y - platform.h / 2 - 10;
            float monsterSize = 40;

            // Check if doodler's position overlaps with the monster
            return (doodler.x + doodler.w / 2 > monsterX - monsterSize / 2 &&
                    doodler.x - doodler.w / 2 < monsterX + monsterSize / 2 &&
                    doodler.y + doodler.h / 2 > monsterY - monsterSize / 2 &&
                    doodler.y - doodler.h / 2 < monsterY + monsterSize / 2);
        }

        Doodler(float x, float y) {
            this.x = x;
            this.y = y;
            w = 40;
            h = 40;
            dy = 0;
            dx = 0;
            score = 0;
            this.highestY = this.y; // Set highestY to the starting y position
            bullets = new ArrayList<>();
        }

        //void calculateScore() {
        // Score based on the highest point reached
        //int currentScore = (int)((HEIGHT - highestY) * 0.1); // Increased sensitivity
        //if (currentScore > score) {
        //  score = currentScore;
        // }
        // }

        void shoot() {
            bullets.add(new Bullet(x, y));
        }
        void updateBullets() {
            for (int i = bullets.size() - 1; i >= 0; i--) {
                Bullet bullet = bullets.get(i);
                bullet.update();
                bullet.show();
                for (Platform platform : platforms) {
                    if (bullet.hitsMonster(platform)) {
                        platform.hasMonster = false; // Remove the monster
                        platform.monsterKilled = true; // Set monsterKilled to true
                        platform.monsterKilledTime = millis(); // Record the time of the kill
                        bullets.remove(i); // Remove the bullet
                        score += 50; // Add 50 points for killing a monster
                        if (!monsterKillSound.isPlaying()) {
                            monsterKillSound.rewind();
                            monsterKillSound.play();
                        }
                        break;
                    }
                }
            }
        }
        void show() {
            pushMatrix(); // Сохраняем текущее состояние матрицы трансформации
            imageMode(CENTER);

            // Отражаем изображение по горизонтали в зависимости от направления
            if (dx < 0) {
                scale(-1, 1); // Flip the image if going left
                if (dy < 0) {
                    image(playerJumpImg, -x, y - h / 2);
                } else {
                    image(playerImg, -x, y - h / 2);
                }
            } else {
                // Проверяем, прыгает ли игрок
                if (dy < 0) {
                    image(playerJumpImg, x, y - h / 2);
                } else {
                    image(playerImg, x, y - h / 2);
                }
            }

            popMatrix(); // Восстанавливаем состояние матрицы трансформации
        }


        boolean lands(Platform p) {
            if (dy > 0) {
                if (x + w / 4 >= p.x - p.w / 2 && x - w / 4 <= p.x + p.w / 2) {
                    if (y + h / 2 >= p.y - p.h / 2 && y + h / 2 <= p.y + p.h / 2) {
                        if (lastPlatformLanded != p) { // Проверка для увеличения счета только при приземлении на новую платформу
                            score += 10;
                            lastPlatformLanded = p; // Обновляем lastPlatformLanded
                        }
                        return true;
                    }
                }
            }
            return false;
        }


        void jump() {
            dy = -20; // или другое значение, которое ты используешь для движения при прыжке
            if (!jumpSound.isPlaying()) {
                jumpSound.rewind(); // Начать с начала, если файл уже проигрывался
                jumpSound.play();
            }
        }

        void move() {
            dy += 1; // simulate gravity
            y += dy; // apply gravity to vertical position
            x += dx; // apply horizontal movement

            // Wraparound from left to right
            if (x > WIDTH) {
                x = 0;
            } else if (x < 0) {
                x = WIDTH;
            }

            if (y < highestY) {
                highestY = y; // Update the highest position
                // calculateScore(); // Re-enable score calculation if needed
            }
            // If the Doodler goes above the top of the screen, reset its upward velocity
            //if (y < 0) {
            //    y = 0; // set the position to the top, so the Doodler doesn't disappear
            //    dy = 0; // stop the upward movement
            //}
            if (y > HEIGHT + h / 2) { // If the Doodler's bottom edge is below the screen
                isGameOver = true;
                if (gameOverSound.isPlaying()) { // Проверяем, не воспроизводится ли звук уже
                    gameOverSound.pause(); // Остановить, если играет
                    gameOverSound.rewind(); // Сбросить в начало
                }
                gameOverSound.play();  // Затем проигрываем звук
                noLoop(); // Stop the draw loop
            }
            if (score >= lastMilestoneScore + 250) {
                lastMilestoneScore += 250; // Increase the threshold by 250 points
                isBlinking = true; // Start the blinking effect
                blinkStartTime = millis(); // Capture the start time for blinking
                blinkState = 0; // Reset the blink state to start with orange

                if (!scoreMilestoneSound.isPlaying()) {
                    scoreMilestoneSound.rewind();
                    scoreMilestoneSound.play();
                }
            }
            // This part of code to run regardless of blinking or not
            fill(255); // белый цвет для текста
            textSize(20); // меньший размер шрифта, чтобы убедиться, что он помещается в углу
            textAlign(LEFT, TOP); // выравнивание текста по верхнему левому углу
            textFont(customFont, 20); // Установите шрифт и размер шрифта перед отображением текста
            text("Счет: " + doodler.score, 10, 10); // отображение счета в верхнем левом углу

            if (isBlinking) {
                updateBlinkState();
            }

            if (score > Doodler.highScore) {
                Doodler.highScore = score;
            }
        }

        void updateBlinkState() {
            long currentTime = millis();
            if (isBlinking) {
                // Calculate elapsed time since last blink started or switched
                int elapsedTime = (int) (currentTime - blinkStartTime);

                // Toggle blink state every 200 milliseconds
                if (elapsedTime >= blinkDuration) {
                    blinkState = 1 - blinkState; // Toggle state: 0 -> 1, 1 -> 0
                    blinkStartTime = currentTime; // Reset blink start time

                    if (blinkState == 0) { // Just turned off the blink
                        blinkCount--; // Decrease blink count
                        if (blinkCount <= 0) {
                            isBlinking = false; // End blinking after specified number of blinks
                            blinkCount = 3; // Reset blink count if you want to start blinking again in future
                        }
                    }
                }

                if (blinkState == 1) {
                    drawScore(color(255, 165, 0)); // Draw text in orange color
                } else {
                    drawScore(color(255, 255, 255)); // Draw text in white color
                }
            } else {
                drawScore(color(255, 255, 255)); // Ensure score is white when not blinking
            }
        }


        void drawScore(int textColor) {
            fill(textColor); // Set the text color based on the parameter
            textSize(20); // Set text size for score
            textAlign(LEFT, TOP); // Align text to the left and top
            textFont(customFont, 20); // Set font and size for the score text
            text("Счет: " + score, 10, 10); // Display the score in the upper left corner
        }
    }

    public class Platform {
        float x, y, w, h, dx;
        boolean hasMonster;
        boolean isMoving;  // Добавляем атрибут isMoving

        boolean monsterKilled = false;
        long monsterKilledTime = 0;


        Platform(float x, float y) {
            this.x = x;
            this.y = y;
            w = 100;
            h = 20;
            dx = 2;  // Устанавливаем скорость движения по горизонтали
            hasMonster = false;
            isMoving = false;  // По умолчанию платформа не движется
        }

        void show() {
            if (isMoving) {  // Если платформа движется
                x += dx;  // Обновляем x на основе dx

                // Если платформа достигает края экрана, меняем направление движения
                if (x + w / 2 > WIDTH || x - w / 2 < 0) {
                    dx = -dx;
                }
            }

            imageMode(CENTER); // Центрируем изображение относительно координат x и y
            image(platformImg, x, y);  // Отображаем изображение платформы вместо рисования прямоугольника

            if (hasMonster) {
                imageMode(CENTER); // Центрируем изображение относительно координат x и y
                image(monsterImg, x, y - h / 2 - 10); // Отображаем изображение врага вместо рисования прямоугольника
            }
            if (monsterKilled) {
                if (millis() - monsterKilledTime < 1000) {
                    imageMode(CENTER);
                    image(pointsImage, x, y - h / 2 - 10);
                } else {
                    monsterKilled = false;
                }
            }
        }
    }
}