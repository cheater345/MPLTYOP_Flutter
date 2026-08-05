import 'package:flutter/material.dart' hide RepeatMode;
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/constants.dart';
import '../../../core/di.dart';
import '../../../domain/entities/track.dart';
import '../../../domain/entities/repeat_mode.dart';

class NowPlayingScreen extends ConsumerWidget {
  const NowPlayingScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(queueControllerProvider);
    final track = state.currentTrack;

    if (track == null) {
      return Scaffold(
        appBar: AppBar(title: const Text('Now Playing')),
        body: const Center(child: Text('Nothing playing')),
      );
    }

    return Scaffold(
      body: CustomScrollView(
        slivers: [
          SliverAppBar(
            expandedHeight: 300,
            pinned: true,
            flexibleSpace: FlexibleSpaceBar(
              background: Container(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    colors: [Colors.grey[800]!, Colors.black],
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                  ),
                ),
                child: Center(
                  child: Hero(
                    tag: 'album-art-${track.id}',
                    child: ClipRRect(
                      borderRadius: BorderRadius.circular(8),
                      child: Image.network(
                        track.thumbnailUrl,
                        fit: BoxFit.cover,
                        width: 250,
                        height: 250,
                      ),
                    ),
                  ),
                ),
              ),
            ),
            title: Text(track.title, maxLines: 1, overflow: TextOverflow.ellipsis),
            leading: IconButton(
              icon: const Icon(Icons.arrow_back),
              onPressed: () => Navigator.pop(context),
            ),
            actions: [
              IconButton(icon: const Icon(Icons.more_vert), onPressed: () {}),
            ],
          ),
          SliverPadding(
            padding: const EdgeInsets.all(24),
            sliver: SliverList(
              delegate: SliverChildListDelegate([
                const SizedBox(height: 16),
                Text(
                  track.title,
                  style: const TextStyle(fontSize: 28, fontWeight: FontWeight.bold),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 8),
                Text(
                  track.artist,
                  style: TextStyle(fontSize: 16, color: Colors.grey[400]),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 32),
                _PlaybackControls(track: track),
                const SizedBox(height: 24),
                _ProgressBar(),
                const SizedBox(height: 24),
                _AdditionalControls(),
                const SizedBox(height: 32),
                _LyricsSection(track: track),
              ]),
            ),
          ),
        ],
      ),
    );
  }
}

class _PlaybackControls extends ConsumerWidget {
  final Track track;
  const _PlaybackControls({required this.track});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(queueControllerProvider);
    final isPlaying = ref.watch(queueControllerProvider.select((s) => false)); // TODO

    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        IconButton(
          icon: const Icon(Icons.shuffle, size: 28),
          onPressed: () => ref.read(queueControllerProvider.notifier).toggleShuffle(),
          color: state.shuffle ? Theme.of(context).colorScheme.primary : null,
        ),
        IconButton(
          icon: const Icon(Icons.skip_previous, size: 40),
          onPressed: () => ref.read(queueControllerProvider.notifier).previous(),
        ),
        Container(
          margin: const EdgeInsets.symmetric(horizontal: 16),
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.primary,
            shape: BoxShape.circle,
          ),
          child: IconButton(
            iconSize: 48,
            icon: Icon(state.currentTrack != null ? Icons.pause : Icons.play_arrow),
            onPressed: () {}, // TODO
          ),
        ),
        IconButton(
          icon: const Icon(Icons.skip_next, size: 40),
          onPressed: () => ref.read(queueControllerProvider.notifier).next(),
        ),
        IconButton(
          icon: const Icon(Icons.repeat, size: 28),
          onPressed: () => ref.read(queueControllerProvider.notifier).cycleRepeat(),
          color: state.repeatMode != RepeatMode.off ? Theme.of(context).colorScheme.primary : null,
        ),
      ],
    );
  }
}

class _ProgressBar extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Column(
      children: [
        Slider(
          value: 0.5, // TODO
          onChanged: (v) {}, // TODO
          activeColor: Theme.of(context).colorScheme.primary,
        ),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('0:00', style: TextStyle(color: Colors.grey[600], fontSize: 12)),
              Text('-3:30', style: TextStyle(color: Colors.grey[600], fontSize: 12)),
            ],
          ),
        ),
      ],
    );
  }
}

class _AdditionalControls extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        IconButton(icon: const Icon(Icons.queue_music), onPressed: () {}),
        IconButton(icon: const Icon(Icons.favorite_border), onPressed: () {}),
        IconButton(icon: const Icon(Icons.lyrics), onPressed: () {}),
        IconButton(icon: const Icon(Icons.devices), onPressed: () {}),
      ],
    );
  }
}

class _LyricsSection extends StatelessWidget {
  final Track track;
  const _LyricsSection({required this.track});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text('Lyrics', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
        const SizedBox(height: 12),
        Container(
          height: 200,
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.grey[900],
            borderRadius: BorderRadius.circular(12),
          ),
          child: const Center(
            child: Text('Lyrics not available', style: TextStyle(color: Colors.grey)),
          ),
        ),
      ],
    );
  }
}