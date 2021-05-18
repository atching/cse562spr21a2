% Alex Ching
% CSE 562 W21 A2

clear all;
close all;
clc;

start = 100;
stop = 129000;

% Read in data
accel = readmatrix('accel.csv');
accelTime = accel(start:stop, 1);
accelX = accel(start:stop, 2);
accelY = accel(start:stop, 3);
accelZ = accel(start:stop, 4);

gravity = readmatrix('gravity.csv');
gravityX = gravity(1:end, 2);
gravityY = gravity(1:end, 3);
gravityZ = gravity(1:end, 4);

gyro = readmatrix('gyro.csv');
gyroTime = gyro(start:stop, 1);
gyroX = gyro(start:stop, 2);
gyroY = gyro(start:stop, 3);
gyroZ = gyro(start:stop, 4);


% %  Plot data to set start/stop
% subplot(1,2,1);
% title('Accelerometer');
% hold on;
% plot(accel(start:stop, 2) - mean(gravityX), 'r');
% plot(accel(start:stop, 3) - mean(gravityY), 'g');
% plot(accel(start:stop, 4) - mean(gravityZ), 'b');
% 
% subplot(1,2,2);
% title('Gyroscope');
% hold on;
% plot(gyro(start:stop, 2), 'r');
% plot(gyro(start:stop, 3), 'g');
% plot(gyro(start:stop, 4), 'b');

% Part 1 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Calculate Bias
gyroXBias = mean(abs(gyroX));
gyroYBias = mean(abs(gyroY));
gyroZBias = mean(abs(gyroZ));

accelXBias = mean(accelX - mean(gravityX));
accelYBias = mean(accelY - mean(gravityY));
accelZBias = mean(accelZ - mean(gravityZ));

% Calculate Noise
gyroXNoise = mean(abs(gyroX - gyroXBias));
gyroYNoise = mean(abs(gyroY - gyroYBias));
gyroZNoise = mean(abs(gyroZ - gyroZBias));

accelXNoise = mean(abs(accelX - mean(gravityX) - accelXBias));
accelYNoise = mean(abs(accelY - mean(gravityY) - accelYBias));
accelZNoise = mean(abs(accelZ - mean(gravityZ) - accelZBias));


% Part 2 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% https://www.analog.com/en/app-notes/an-1057.html

% Calculate Tilt from Accel for non moving phone

tiltAccelX = zeros(length(accelX)-1, 1);
tiltAccelY = zeros(length(accelX)-1, 1);

gravity = sqrt(mean(gravityX)^2 + mean(gravityY)^2 + mean(gravityZ)^2);

for i = 1:length(accelX)
    tiltAccelX(i) = asin(accelX(i)/gravity);
    tiltAccelY(i) = asin(accelY(i)/gravity);
end


% Calculate Tilt from Gyro for non moving phone

tiltGyroX = zeros(length(gyroX)-1, 1);
tiltGyroY = zeros(length(gyroX)-1, 1);
tiltGyroZ = zeros(length(gyroX)-1, 1);

for i = 2:length(gyroX)
    deltaTime = (gyroTime(i) - gyroTime(i-1))/1000000000; % Delta Time in ns
    tiltGyroX(i) =  mean([gyroX(i) gyroX(i-1)])*deltaTime + tiltGyroX(i-1);
    tiltGyroY(i) =  mean([gyroY(i) gyroY(i-1)])*deltaTime + tiltGyroY(i-1);
    tiltGyroZ(i) =  mean([gyroZ(i) gyroZ(i-1)])*deltaTime + tiltGyroZ(i-1);
end

timeA = (accelTime - accelTime(1))/1000000000;

hold on;
subplot(1,2,1);
sgtitle('Angle Calculated Using Only Accelerometer');
plot(timeA, tiltAccelX*180/pi);
title('X');
xlabel('Time (sec)');
ylabel('Angle (deg)');
subplot(1,2,2);
plot(timeA, tiltAccelY*180/pi);
title('Y');
xlabel('Time (sec)');
ylabel('Angle (deg)');

timeG = (gyroTime - gyroTime(1))/1000000000;
figure;
hold on;
plot(timeG, tiltGyroX*180/pi, 'r');
plot(timeG, tiltGyroY*180/pi, 'g');
% plot(timeG, tiltGyroZ*180/pi, 'b');
title('Angle Calculated Using Only Gyroscope');
legend('X', 'Y');
xlabel('Time (sec)');
ylabel('Angle (deg)');


% Complemntary Filter

tiltComboX = zeros(length(tiltGyroX)-1, 1);
tiltComboY = zeros(length(tiltGyroY)-1, 1);

b = 0.95;
for i = 1:length(tiltGyroX)
    tiltComboX(i) =  tiltGyroX(i)*b + tiltAccelX(i)*(1-b);
    tiltComboY(i) =  tiltGyroY(i)*b + tiltAccelY(i)*(1-b);
end


figure;
hold on;
plot(timeG, tiltComboX*180/pi, 'r');
plot(timeG, tiltComboY*180/pi, 'b');
title('Angle Calculated Using Complementary Filter');
xlabel('Time (sec)');
ylabel('Angle (deg)');



