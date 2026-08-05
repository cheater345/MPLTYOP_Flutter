import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/constants.dart';
import '../../providers/settings_controller.dart';

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final settings = ref.watch(settingsControllerProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: settings.isLoading
          ? const Center(child: CircularProgressIndicator())
          : ListView(
              padding: const EdgeInsets.all(16),
              children: [
                _sectionTitle('Appearance'),
                ListTile(
                  title: const Text('Theme'),
                  subtitle: Text(_themeModeLabel(settings.themeMode)),
                  trailing: DropdownButton<ThemeMode>(
                    value: settings.themeMode,
                    items: ThemeMode.values.map((m) => DropdownMenuItem(
                      value: m,
                      child: Text(_themeModeLabel(m)),
                    )).toList(),
                    onChanged: (v) => v != null && ref.read(settingsControllerProvider.notifier).setThemeMode(v),
                  ),
                ),
                _sectionTitle('Playback'),
                ListTile(
                  title: const Text('Audio Quality'),
                  subtitle: Text(settings.audioQuality.name.toUpperCase()),
                  trailing: DropdownButton<AudioQuality>(
                    value: settings.audioQuality,
                    items: AudioQuality.values.map((q) => DropdownMenuItem(
                      value: q,
                      child: Text(q.name.toUpperCase()),
                    )).toList(),
                    onChanged: (v) => v != null && ref.read(settingsControllerProvider.notifier).setAudioQuality(v),
                  ),
                ),
                ListTile(
                  title: const Text('Thumbnail Quality'),
                  subtitle: Text(settings.thumbnailQuality.name.toUpperCase()),
                  trailing: DropdownButton<ThumbnailQuality>(
                    value: settings.thumbnailQuality,
                    items: ThumbnailQuality.values.map((q) => DropdownMenuItem(
                      value: q,
                      child: Text(q.name.toUpperCase()),
                    )).toList(),
                    onChanged: (v) => v != null && ref.read(settingsControllerProvider.notifier).setThumbnailQuality(v),
                  ),
                ),
                SwitchListTile(
                  title: const Text('Auto-play next'),
                  value: settings.autoPlay,
                  onChanged: (v) => ref.read(settingsControllerProvider.notifier).setAutoPlay(v),
                ),
                _sectionTitle('Storage'),
                SwitchListTile(
                  title: const Text('Clear cache on exit'),
                  value: settings.clearCacheOnExit,
                  onChanged: (v) => ref.read(settingsControllerProvider.notifier).setClearCacheOnExit(v),
                ),
                ListTile(
                  title: const Text('Clear cache now'),
                  leading: const Icon(Icons.delete_outline),
                  onTap: () async {
                    await ref.read(settingsControllerProvider.notifier).clearCache();
                    if (context.mounted) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Cache cleared')),
                      );
                    }
                  },
                ),
                _sectionTitle('Advanced'),
                ListTile(
                  title: const Text('Country'),
                  subtitle: Text(settings.country),
                  trailing: DropdownButton<String>(
                    value: settings.country,
                    items: ['US', 'GB', 'CA', 'AU', 'DE', 'FR', 'JP', 'KR'].map((c) => DropdownMenuItem(
                      value: c,
                      child: Text(c),
                    )).toList(),
                    onChanged: (v) => v != null && ref.read(settingsControllerProvider.notifier).setCountry(v),
                  ),
                ),
                _sectionTitle('About'),
                const ListTile(
                  title: Text('Version'),
                  subtitle: Text('1.0.0'),
                ),
                ListTile(
                  title: const Text('Reset to defaults'),
                  leading: const Icon(Icons.restore, color: Colors.red),
                  onTap: () async {
                    await ref.read(settingsControllerProvider.notifier).resetToDefaults();
                    if (context.mounted) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Settings reset')),
                      );
                    }
                  },
                ),
              ],
            ),
    );
  }

  String _themeModeLabel(ThemeMode mode) => switch (mode) {
    ThemeMode.light => 'Light',
    ThemeMode.dark => 'Dark',
    ThemeMode.system => 'System',
  };

  Widget _sectionTitle(String title) => Padding(
    padding: const EdgeInsets.only(top: 16, bottom: 8),
    child: Text(title, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: Colors.grey)),
  );
}