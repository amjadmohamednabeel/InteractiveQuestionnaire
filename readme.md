# Interactive Video Questionnaire

An Android application that provides an interactive video-based questionnaire system for personalized product recommendations. Built with Kotlin, Jetpack Compose, and ExoPlayer.

## 📱 Overview

This application guides users through a multi-step questionnaire using video content, collecting user preferences based on gender, age group, and lifestyle, then presents personalized product recommendations through interactive video zones.

## ✨ Features

- **Touch-based Video Navigation**: Interactive video player with grid-based touch detection
- **Multi-step Questionnaire Flow**: 
  - Start screen
  - Gender selection
  - Age group selection  
  - Lifestyle selection
  - Problem-specific questions
  - Personalized results
- **Dynamic Product Recommendations**: 70 unique video combinations based on user selections
- **Interactive Product Zones**: Clickable areas on result videos that play product-specific content
- **Automatic Timeout Management**: Returns to start screen after periods of inactivity
- **Edge-to-edge Display**: Full-screen immersive experience

## 🏗️ Architecture

### Core Components

- **MainActivity**: Entry point with window configuration and content setup
- **VideoTouchSelector**: Main composable handling video playback and touch interactions
- **ExoPlayer Integration**: Video playback with custom controls and listeners

### Data Models

```kotlin
sealed class Gender {
    object Male : Gender()
    object Female : Gender()
}

sealed class AgeGroup {
    object Young : AgeGroup()      // 18-30
    object Middle : AgeGroup()     // 30-45
    object Senior : AgeGroup()     // above45
}

sealed class Lifestyle {
    object Sedentary : Lifestyle()
    object Normal : Lifestyle()
    object Athlete : Lifestyle()
}

data class UserSelection(
    val gender: Gender?,
    val ageGroup: AgeGroup?,
    val lifestyle: Lifestyle?,
    val problemOption: Int?
)
```

## 🎯 User Flow

1. **Start Screen**: Welcome video with center touch to begin
2. **Gender Selection**: Choose between Male/Female options
3. **Age Selection**: Select from three age ranges (18-30, 30-45, 45+)
4. **Lifestyle Selection**: Choose activity level (Sedentary, Normal, Athlete)
5. **Problem Selection**: Select from 3-5 options based on previous selections
6. **Results**: View personalized recommendation video with interactive product zones

## 🎮 Touch Interaction System

The application uses a 6x6 grid system for touch detection on the result screen and 3x3 grid for other screens:

### Grid Mapping Examples:
- **Start Screen**: Center cell (index 4) to proceed
- **Gender Screen**: Left for Female (index 6), Right for Male (index 8)
- **Problem Screen**: Variable options (3-5) based on user profile
- **Result Screen**: Product zones mapped to specific grid areas

## 📹 Video Resources

The application requires the following video resources in `res/raw/`:

### Core Navigation Videos
- `start.mp4` - Welcome/start screen
- `gender.mp4` - Gender selection screen
- `age.mp4` - Age selection screen  
- `lifestyle.mp4` - Lifestyle selection screen

### Problem Videos (18 variations)
- `p1.mp4` to `p18.mp4` - Problem selection videos for each user profile combination

### Result Videos (70 variations)
- `v1.mp4` to `v70.mp4` - Personalized recommendation videos

### Product Videos
Various product-specific videos including:
- Supplements (co_q10_200, super_aktiv, ashwagandha, etc.)
- Vitamins and minerals
- Specialized health products

## ⚙️ Configuration Constants

```kotlin
private object Constants {
    const val INACTIVITY_TIMEOUT = 12000L // 12 seconds
    const val TOUCH_ENABLE_DELAY = 800L    // 0.8 seconds  
    const val STATE_CHANGE_DELAY = 4000L   // 4 seconds
}
```

## 🔧 Technical Requirements

### Dependencies
- **Jetpack Compose**: UI framework
- **ExoPlayer**: Video playback (`androidx.media3`)
- **Kotlin Coroutines**: Asynchronous operations
- **Android Core KTX**: Android extensions

### Minimum SDK
- Target SDK: Latest Android version
- Minimum SDK: Android 5.0+ (API 21)

### Permissions
The app requires access to video resources stored in the application package.

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Kotlin 1.8+
- Android SDK 21+

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/interactive-video-questionnaire.git
```

2. Open the project in Android Studio

3. Add all required video files to `app/src/main/res/raw/` directory

4. Build and run the application:
```bash
./gradlew assembleDebug
```

### Video File Setup
Ensure all video files are properly named and placed in the `res/raw/` directory:
- Format: MP4 recommended
- Naming: Exact match with resource IDs in the code
- Quality: Optimized for mobile playback

## 🎨 UI/UX Features

- **Immersive Experience**: Edge-to-edge display with hidden system bars
- **Touch Feedback**: Visual and behavioral responses to user interactions
- **Smooth Transitions**: Seamless video switching between states
- **Timeout Handling**: Automatic return to start after inactivity
- **Responsive Design**: Adapts to different screen sizes and orientations

## 📊 Product Recommendation Logic

The application uses a sophisticated mapping system that considers:
- **User Demographics**: Gender, age group, lifestyle
- **Problem Selection**: Specific health/wellness concerns
- **Product Zones**: Interactive areas within result videos
- **Sequential Mapping**: 70 unique combinations for personalized recommendations

## 🔄 State Management

The application manages multiple states:
- **Navigation States**: start → gender → age → lifestyle → problem → result
- **Touch States**: Enabled/disabled based on video transitions
- **Video States**: Playing, ended, looping
- **Timeout States**: Activity tracking and automatic reset

## 🛠️ Customization

### Adding New Products
1. Add product video to `res/raw/`
2. Update `productGridMap` with touch zones
3. Map zones to appropriate result videos

### Modifying User Flow
1. Update state transitions in `VideoTouchSelector`
2. Adjust grid mappings for touch detection
3. Update video resource mappings

### Timeout Adjustment
Modify constants in the `Constants` object to adjust timing behavior.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Amjad Mohamed Nabeel** - *Initial work* - [GitHub Profile](https://github.com/amjadnabeel)

## 📞 Support

For support, email amjad@example.com or create an issue in this repository.

## 🙏 Acknowledgments

- ExoPlayer team for excellent video playback capabilities
- Jetpack Compose team for modern UI framework
- Android development community for continuous support

---

**Version**: 1.0.0  
**Last Updated**: August 2025  
**Status**: Active Development