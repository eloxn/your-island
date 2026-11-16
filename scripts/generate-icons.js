const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

const inputPath = 'C:/Users/Eloxxn/.gemini/antigravity/brain/fc96eed1-ee76-43df-b7ce-88671930658e/.user_uploaded/media_1794806851478_81fa54cb.jpg';

async function processIcons() {
  console.log('Generating logo and app icon assets...');
  
  // 1. Root & public logo
  await sharp(inputPath).png().toFile('logo.png');
  await sharp(inputPath).resize(512, 512).png().toFile('public/logo.png');

  // 2. Android drawable app_logo
  const drawableDir = 'android/app/src/main/res/drawable';
  await sharp(inputPath).resize(512, 512).png().toFile(path.join(drawableDir, 'app_logo.png'));

  // 3. Adaptive icon foreground (432x432 with 72% inner scale so it fits nicely inside the safe zone mask)
  const innerSize = Math.round(432 * 0.72);
  const innerBuffer = await sharp(inputPath).resize(innerSize, innerSize).png().toBuffer();
  await sharp({
    create: {
      width: 432,
      height: 432,
      channels: 4,
      background: { r: 0, g: 0, b: 0, alpha: 0 }
    }
  })
  .composite([{ input: innerBuffer, gravity: 'center' }])
  .png()
  .toFile(path.join(drawableDir, 'ic_launcher_foreground.png'));

  // 4. Mipmaps
  const sizes = [
    { dir: 'mipmap-mdpi', size: 48 },
    { dir: 'mipmap-hdpi', size: 72 },
    { dir: 'mipmap-xhdpi', size: 96 },
    { dir: 'mipmap-xxhdpi', size: 144 },
    { dir: 'mipmap-xxxhdpi', size: 192 }
  ];

  for (const item of sizes) {
    const targetDir = path.join('android/app/src/main/res', item.dir);
    if (!fs.existsSync(targetDir)) fs.mkdirSync(targetDir, { recursive: true });
    
    // Regular icon
    await sharp(inputPath)
      .resize(item.size, item.size)
      .webp({ quality: 95 })
      .toFile(path.join(targetDir, 'ic_launcher.webp'));

    // Round icon (circle mask)
    const circleSvg = Buffer.from(
      `<svg><circle cx="${item.size / 2}" cy="${item.size / 2}" r="${item.size / 2}" /></svg>`
    );
    await sharp(inputPath)
      .resize(item.size, item.size)
      .composite([{ input: circleSvg, blend: 'dest-in' }])
      .webp({ quality: 95 })
      .toFile(path.join(targetDir, 'ic_launcher_round.webp'));

    console.log(`Generated ${item.dir} (${item.size}x${item.size})`);
  }

  console.log('All icons generated successfully!');
}

processIcons().catch(console.error);
