# Elkhalfy Android App

تطبيق Android لمشاهدة القنوات المباشرة والأفلام والمسلسلات بتصميم احترافي.

## المميزات
- 📺 **قنوات مباشرة** - بث مباشر HLS عالي الجودة
- 🎬 **أفلام** - مشغل VOD متكامل
- 📺 **مسلسلات** - مع عرض المواسم والحلقات
- ⚽ **كرة القدم** - مباريات، ترتيب، هدافون
- 🔥 **ExoPlayer/Media3** - أفضل مشغل لـ Android

## بناء APK عبر GitHub

### الخطوات:

1. **ارفع المشروع إلى GitHub:**
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin https://github.com/YOUR_USERNAME/elkhalfy-android.git
   git push -u origin main
   ```

2. **GitHub Actions تبني APK تلقائياً** عند كل push

3. **حمّل APK من:**
   - `Actions` → اختر آخر run → `Artifacts` → `Elkhalfy-debug-apk`

## Firebase Configuration
البيانات مدمجة في `app/google-services.json` - لا حاجة لتغيير أي شيء.

## Stack
- **Kotlin** - لغة البرمجة
- **ExoPlayer / Media3** - تشغيل HLS, RTSP, MP4
- **Firebase Firestore** - إعدادات التطبيق والسيرفرات
- **Glide** - تحميل الصور
- **OkHttp + Gson** - API calls
- **Navigation Component** - التنقل بين الشاشات
- **Material Design** - واجهة المستخدم
