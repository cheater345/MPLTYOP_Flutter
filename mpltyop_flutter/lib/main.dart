import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:just_audio_background/just_audio_background.dart';

import 'core/constants.dart';
import 'core/di.dart';
import 'core/theme/dark_theme.dart';
import 'core/theme/light_theme.dart';
import 'presentation/screens/home/home_screen.dart';
import 'presentation/screens/search/search_screen.dart';
import 'presentation/screens/library/library_screen.dart';
import 'presentation/screens/playlist_detail/playlist_detail_screen.dart';
import 'presentation/screens/now_playing/now_playing_screen.dart';
import 'presentation/screens/settings/settings_screen.dart';
import 'presentation/widgets/player_bar.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await runZonedGuarded(() => _bootstrap(), (error, stack) {
    debugPrint('FATAL (zone): $error\n$stack');
    _showFatalError(error, stack);
  });
}

Future<void> _bootstrap() async {
  try {
    await Hive.initFlutter();

    // Open boxes defensively: a corrupt/incompatible box must never
    // block startup with a black screen.
    for (final name in [
      AppConstants.boxTracks,
      AppConstants.boxPlaylists,
      AppConstants.boxLiked,
      AppConstants.boxQueue,
      AppConstants.boxHistory,
      AppConstants.boxSettings,
    ]) {
      try {
        await Hive.openBox(name);
      } catch (e) {
        debugPrint('Hive box "$name" failed to open: $e - recreating');
        await Hive.deleteBoxFromDisk(name);
        await Hive.openBox(name);
      }
    }

    // This also calls AudioService.init internally, so no separate
    // AudioService.init should be called (doing so would create a second
    // PlaybackService/AudioPlayer, which just_audio_background forbids).
    await JustAudioBackground.init(
      androidNotificationChannelId: 'com.cheater345.mpltyop.channel.audio',
      androidNotificationChannelName: 'MPLTYOP Audio',
      androidNotificationOngoing: true,
      androidStopForegroundOnPause: true,
    );

    runApp(const ProviderScope(child: MPLTYOPApp()));
  } catch (e, stack) {
    debugPrint('FATAL (bootstrap): $e\n$stack');
    _showFatalError(e, stack);
  }
}

void _showFatalError(Object error, StackTrace? stack) {
  runApp(StartupErrorScreen(error: error, stack: stack));
}

class StartupErrorScreen extends StatelessWidget {
  final Object error;
  final StackTrace? stack;
  const StartupErrorScreen({super.key, required this.error, this.stack});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        backgroundColor: Colors.white,
        body: SafeArea(
          child: Center(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Icon(Icons.error_outline, color: Colors.red, size: 48),
                  const SizedBox(height: 16),
                  const Text(
                    'Startup error',
                    style: TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.bold,
                      color: Colors.black,
                    ),
                  ),
                  const SizedBox(height: 16),
                  SelectableText(
                    '$error',
                    style: const TextStyle(fontSize: 15, color: Colors.black87),
                  ),
                  if (stack != null) ...[
                    const SizedBox(height: 16),
                    SelectableText(
                      '$stack',
                      style: const TextStyle(
                        fontSize: 12,
                        color: Colors.black54,
                        fontFamily: 'monospace',
                      ),
                    ),
                  ],
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class MPLTYOPApp extends ConsumerWidget {
  const MPLTYOPApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final settings = ref.watch(settingsControllerProvider);
    
    return MaterialApp(
      title: AppConstants.appName,
      debugShowCheckedModeBanner: false,
      theme: lightTheme,
      darkTheme: darkTheme,
      themeMode: settings.themeMode,
      home: const MainNavigation(),
      routes: {
        '/now-playing': (context) => const NowPlayingScreen(),
        '/playlist': (context) => const PlaylistDetailScreen(),
        '/settings': (context) => const SettingsScreen(),
      },
    );
  }
}

class MainNavigation extends ConsumerStatefulWidget {
  const MainNavigation({super.key});

  @override
  ConsumerState<MainNavigation> createState() => _MainNavigationState();
}

class _MainNavigationState extends ConsumerState<MainNavigation> {
  int _selectedIndex = 0;
  final _screens = [
    const HomeScreen(),
    const SearchScreen(),
    const LibraryScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Row(
        children: [
          NavigationRail(
            selectedIndex: _selectedIndex,
            onDestinationSelected: (index) => setState(() => _selectedIndex = index),
            labelType: NavigationRailLabelType.all,
            destinations: const [
              NavigationRailDestination(
                icon: Icon(Icons.home_outlined),
                selectedIcon: Icon(Icons.home),
                label: Text('Home'),
              ),
              NavigationRailDestination(
                icon: Icon(Icons.search_outlined),
                selectedIcon: Icon(Icons.search),
                label: Text('Search'),
              ),
              NavigationRailDestination(
                icon: Icon(Icons.library_music_outlined),
                selectedIcon: Icon(Icons.library_music),
                label: Text('Library'),
              ),
            ],
          ),
          const VerticalDivider(thickness: 1, width: 1),
          Expanded(
            child: _screens[_selectedIndex],
          ),
        ],
      ),
      bottomNavigationBar: const PlayerBar(),
    );
  }
}
