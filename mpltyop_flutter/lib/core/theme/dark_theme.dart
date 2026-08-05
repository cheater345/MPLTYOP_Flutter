import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../constants.dart';

ThemeData darkTheme = ThemeData(
  useMaterial3: true,
  brightness: Brightness.dark,
  colorScheme: const ColorScheme.dark(
    primary: Color(0xFF1ED760),      // Spotify green
    onPrimary: Colors.black,
    secondary: Color(0xFF1DB954),
    onSecondary: Colors.black,
    surface: Color(0xFF121212),
    onSurface: Color(0xFFFFFFFF),
    surfaceContainerHighest: Color(0xFF282828),
    outline: Color(0xFF404040),
    error: Color(0xFFCF6679),
  ),
  textTheme: GoogleFonts.interTextTheme(ThemeData.dark().textTheme).apply(
    bodyColor: Colors.white,
    displayColor: Colors.white,
  ),
  scaffoldBackgroundColor: const Color(0xFF0B0B0C),
  cardColor: const Color(0xFF181818),
  dividerColor: const Color(0xFF282828),
  appBarTheme: const AppBarTheme(
    backgroundColor: Color(0xFF121213),
    surfaceTintColor: Colors.transparent,
    elevation: 0,
    centerTitle: false,
    titleTextStyle: TextStyle(
      color: Colors.white,
      fontSize: 20,
      fontWeight: FontWeight.bold,
    ),
    iconTheme: IconThemeData(color: Colors.white),
  ),
  bottomNavigationBarTheme: const BottomNavigationBarThemeData(
    backgroundColor: Color(0xFF121213),
    selectedItemColor: Color(0xFF1ED760),
    unselectedItemColor: Colors.grey,
    type: BottomNavigationBarType.fixed,
  ),
  navigationRailTheme: const NavigationRailThemeData(
    backgroundColor: Color(0xFF0B0B0C),
    selectedIconTheme: IconThemeData(color: Color(0xFF1ED760)),
    unselectedIconTheme: IconThemeData(color: Colors.grey),
    selectedLabelTextStyle: TextStyle(color: Color(0xFF1ED760), fontWeight: FontWeight.w600),
    unselectedLabelTextStyle: TextStyle(color: Colors.grey),
  ),
  elevatedButtonTheme: ElevatedButtonThemeData(
    style: ElevatedButton.styleFrom(
      backgroundColor: const Color(0xFF1ED760),
      foregroundColor: Colors.black,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
    ),
  ),
  inputDecorationTheme: InputDecorationTheme(
    filled: true,
    fillColor: const Color(0xFF242426),
    border: OutlineInputBorder(
      borderRadius: BorderRadius.circular(24),
      borderSide: BorderSide.none,
    ),
    hintStyle: const TextStyle(color: Colors.grey),
    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
  ),
  sliderTheme: SliderThemeData(
    activeTrackColor: const Color(0xFF1ED760),
    inactiveTrackColor: Colors.grey[800],
    thumbColor: Colors.white,
    overlayColor: const Color(0xFF1ED760).withOpacity(0.2),
    trackHeight: 4,
    thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 8),
  ),
  dialogTheme: DialogThemeData(
    backgroundColor: const Color(0xFF1A1A1C),
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
  ),
  bottomSheetTheme: const BottomSheetThemeData(
    backgroundColor: Color(0xFF1A1A1C),
    shape: RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
  ),
);