import numpy as np, wave, sys
SR = 22050
rng = np.random.default_rng(42)

def t_arr(d): return np.arange(int(d*SR))/SR
def note_freq(m): return 440.0*2**((m-69)/12)
def saw(f, t, harm=8):
    s = np.zeros_like(t)
    for k in range(1, harm+1):
        s += np.sin(2*np.pi*f*k*t)/k
    return s*0.6
def env_ad(n, a, d_rate):
    t = np.arange(n)/SR
    return np.minimum(1, t/max(a,1e-4))*np.exp(-t*d_rate)
def place(buf, snd, start):
    i = int(start*SR)
    if i >= len(buf): return
    n = min(len(snd), len(buf)-i)
    buf[i:i+n] += snd[:n]
def lowpass(x, a):
    y = np.zeros_like(x); s = 0.0
    for i in range(len(x)):
        s += a*(x[i]-s); y[i] = s
    return y
def kick(d=0.35):
    t = t_arr(d); f = 45+110*np.exp(-t*28)
    ph = 2*np.pi*np.cumsum(f)/SR
    return np.sin(ph)*np.exp(-t*9)
def hat(d=0.06):
    n = rng.uniform(-1,1,int(d*SR)); n = np.diff(n, prepend=0)
    return n*np.exp(-np.arange(len(n))/SR*60)*0.4
def finish(name, buf):
    D = len(buf)/SR
    fade = np.minimum(1, np.minimum(np.arange(len(buf))/(0.05*SR), (len(buf)-np.arange(len(buf)))/(1.5*SR)))
    buf = buf*fade
    buf = buf/np.max(np.abs(buf))*0.8
    with wave.open(f"/tmp/nhintro/NHIntroStudio/app/src/main/assets/audio/{name}.wav", "wb") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR)
        w.writeframes((buf*32767).astype(np.int16).tobytes())
    print(name, round(D,1), "s")

D = 14.0
N = int(D*SR)

# 1 Cinematic Pulse: 90bpm sub pulse, minor drone, rising swell, impacts
b = np.zeros(N); beat = 60/90
t = t_arr(D)
for k in range(int(D/beat)):
    place(b, np.sin(2*np.pi*55*t_arr(0.5))*np.exp(-t_arr(0.5)*7)*0.9, k*beat)
for f in (110, 130.81, 164.81, 82.41):
    b += np.sin(2*np.pi*f*t + 0.4*np.sin(2*np.pi*0.2*t))*0.12*(0.7+0.3*np.sin(2*np.pi*0.13*t+f))
sw = lowpass(rng.uniform(-1,1,N), 0.05)*np.minimum(1, t/ D)**2*1.6
b += sw*0.5
for k in range(0, int(D/(beat*4))):
    place(b, kick(0.8)*1.1, k*beat*4)
    place(b, lowpass(rng.uniform(-1,1,int(1.5*SR)), 0.2)*np.exp(-t_arr(1.5)*3)*0.5, k*beat*4)
finish("cinematic_pulse", b)

# 2 Neon Drive: 120bpm synthwave arpeggio
b = np.zeros(N); beat = 0.5; step = beat/4
prog = [(57,60,64),(53,57,60),(48,52,55),(55,59,62)]
for s in range(int(D/step)):
    chord = prog[(s//16) % 4]
    m = chord[[0,1,2,1][s%4]] + (12 if (s//4)%2 else 0) + 12
    f = note_freq(m); n = int(step*1.6*SR); tt = np.arange(n)/SR
    place(b, saw(f, tt, 6)*env_ad(n,0.004,6)*0.22, s*step)
for s in range(int(D/(beat/2))):
    chord = prog[(s//8) % 4]
    f = note_freq(chord[0]-12); n = int(beat/2*SR); tt = np.arange(n)/SR
    place(b, np.sin(2*np.pi*f*tt)*env_ad(n,0.003,4)*0.45 + saw(f,tt,4)*env_ad(n,0.003,5)*0.12, s*beat/2)
for k in range(int(D/beat)):
    place(b, kick()*0.95, k*beat)
    place(b, hat(), k*beat+beat/2)
finish("neon_drive", b)

# 3 Royal Strings: slow detuned chords + bells
b = np.zeros(N)
chords = [(50,53,57,62),(46,50,53,58),(53,57,60,65),(48,52,55,60)]
for i, ch in enumerate(chords):
    start = i*3.5; dur = 4.4
    n = int(dur*SR); tt = np.arange(n)/SR
    e = np.minimum(1, tt/0.9)*np.minimum(1, (dur-tt)/1.2)
    snd = np.zeros(n)
    for m in ch:
        for det in (-0.12, 0.0, 0.12):
            snd += saw(note_freq(m)*(1+det/100*3), tt, 10)*0.05
    place(b, lowpass(snd*e, 0.35), start)
    place(b, np.sin(2*np.pi*note_freq(ch[0]-12)*tt)*e*0.25, start)
for k, m in enumerate([81,84,88,86,84,79,76,84]):
    n = int(1.5*SR); tt = np.arange(n)/SR
    place(b, (np.sin(2*np.pi*note_freq(m)*tt)+0.3*np.sin(2*np.pi*note_freq(m)*2.01*tt))*np.exp(-tt*3)*0.12, 1.0+k*1.6)
finish("royal_strings", b)

# 4 Deep Space: ambient drones, swells, sparse bells
b = np.zeros(N); t = t_arr(D)
for f, a in ((55,0.35),(82.41,0.25),(110,0.2),(164.81,0.1)):
    b += np.sin(2*np.pi*f*t + 1.5*np.sin(2*np.pi*0.07*t+f))*a*(0.6+0.4*np.sin(2*np.pi*0.11*t+f*0.1))
for k in range(3):
    n = int(5*SR); tt = np.arange(n)/SR
    sw = lowpass(rng.uniform(-1,1,n), 0.04)*np.sin(np.pi*tt/5)**2*1.3
    place(b, sw*0.5, k*4.2)
for k, m in enumerate([84,91,88,96,79,91]):
    n = int(3*SR); tt = np.arange(n)/SR
    place(b, np.sin(2*np.pi*note_freq(m)*tt)*np.exp(-tt*1.6)*0.14, 0.8+k*2.1)
finish("deep_space", b)

# 5 Logo AI Minimal: modern crystal plucks, warm sub-bass swell, airy shimmer pad
b = np.zeros(N); t = t_arr(D)
for f, a in ((110.0, 0.22), (164.81, 0.16), (220.0, 0.14), (277.18, 0.10)):
    b += np.sin(2*np.pi*f*t)*a*(0.65+0.35*np.sin(2*np.pi*0.25*t))*np.minimum(1, t/0.6)
notes = [69, 73, 76, 81, 85, 81, 76, 88]
for idx, m in enumerate(notes):
    st = 0.25 + idx * 0.55
    n = int(1.4*SR); tt = np.arange(n)/SR
    f = note_freq(m)
    pluck = (np.sin(2*np.pi*f*tt)*0.45 + np.sin(2*np.pi*f*2*tt)*0.18)*np.exp(-tt*4.2)
    place(b, pluck, st)
finish("logo_ai_minimal", b)
