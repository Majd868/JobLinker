"""
JobLinker Project Book Generator
Generates a complete professional PDF book documenting the JobLinker Android app.
"""

import json, os, io, textwrap
from PIL import Image as PILImage, ImageDraw, ImageFont
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import cm, mm
from reportlab.lib.colors import HexColor, white, black, Color
from reportlab.platypus import (
    BaseDocTemplate, PageTemplate, Frame,
    Paragraph, Spacer, PageBreak, Table, TableStyle,
    Image as RLImage, KeepTogether, HRFlowable, NextPageTemplate
)
from reportlab.platypus.tableofcontents import TableOfContents
from reportlab.lib import colors
from reportlab.graphics.shapes import Drawing, Rect, String, Line, Circle
from reportlab.graphics import renderPDF
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont

# ─────────────────────────────────────────────
# COLORS
# ─────────────────────────────────────────────
C_PRIMARY   = HexColor('#1565C0')   # Deep Blue
C_ACCENT    = HexColor('#42A5F5')   # Light Blue
C_DARK      = HexColor('#0D1B2A')   # Near Black
C_SURFACE   = HexColor('#F5F7FA')   # Light Gray
C_CODE_BG   = HexColor('#1E1E2E')   # Dark code bg
C_CODE_KW   = HexColor('#BB86FC')   # Keyword purple
C_CODE_STR  = HexColor('#A3E635')   # String green
C_CODE_CMT  = HexColor('#6A737D')   # Comment gray
C_PHONE_FRM = HexColor('#1A1A2E')   # Phone frame
C_PHONE_SCR = HexColor('#FAFAFA')   # Screen bg
C_TOOLBAR   = HexColor('#1565C0')   # Toolbar blue
C_DIVIDER   = HexColor('#E0E0E0')   # Divider
C_BADGE     = HexColor('#E53935')   # Badge red
C_GREEN     = HexColor('#2E7D32')
C_ORANGE    = HexColor('#E65100')

PAGE_W, PAGE_H = A4

# ─────────────────────────────────────────────
# STYLES
# ─────────────────────────────────────────────
styles = getSampleStyleSheet()

def make_style(name, **kw):
    base = kw.pop('parent', 'Normal')
    s = ParagraphStyle(name, parent=styles[base], **kw)
    styles.add(s)
    return s

ST_TITLE      = make_style('BookTitle',    fontSize=32, textColor=white,      alignment=1, leading=40, fontName='Helvetica-Bold')
ST_SUBTITLE   = make_style('BookSubtitle', fontSize=16, textColor=C_ACCENT,   alignment=1, leading=22, fontName='Helvetica')
ST_H1         = make_style('H1',           fontSize=20, textColor=C_PRIMARY,  spaceBefore=14, spaceAfter=8, fontName='Helvetica-Bold', leading=26)
ST_H2         = make_style('H2',           fontSize=15, textColor=C_DARK,     spaceBefore=10, spaceAfter=5, fontName='Helvetica-Bold', leading=20)
ST_H3         = make_style('H3',           fontSize=12, textColor=C_PRIMARY,  spaceBefore=7,  spaceAfter=3, fontName='Helvetica-Bold', leading=16)
ST_BODY       = make_style('Body',         fontSize=10, textColor=C_DARK,     spaceAfter=5,  leading=15,   fontName='Helvetica')
ST_BODY_SMALL = make_style('BodySmall',    fontSize=9,  textColor=C_DARK,     spaceAfter=3,  leading=13,   fontName='Helvetica')
ST_CODE       = make_style('CodeBlock',    fontSize=7.5,textColor=HexColor('#ABB2BF'), backColor=C_CODE_BG,
                            fontName='Courier', leading=11, leftIndent=8, rightIndent=8, spaceBefore=2, spaceAfter=2)
ST_CAPTION    = make_style('Caption',      fontSize=8.5,textColor=HexColor('#546E7A'), alignment=1, fontName='Helvetica-Oblique', spaceAfter=6)
ST_TABLE_HDR  = make_style('TableHdr',     fontSize=9, textColor=white, fontName='Helvetica-Bold', alignment=1)
ST_TABLE_CELL = make_style('TableCell',    fontSize=8.5, textColor=C_DARK, fontName='Helvetica', leading=12)
ST_BULLET     = make_style('BulletBody',   fontSize=10, textColor=C_DARK, leftIndent=14, spaceAfter=3,
                            fontName='Helvetica', bulletIndent=4, leading=14)
ST_SCREEN_TIT = make_style('ScreenTitle',  fontSize=13, textColor=C_PRIMARY, fontName='Helvetica-Bold', spaceAfter=4, leading=17)
ST_ELEM_NAME  = make_style('ElemName',     fontSize=9, textColor=C_PRIMARY, fontName='Helvetica-Bold', leading=13)
ST_ELEM_DESC  = make_style('ElemDesc',     fontSize=9, textColor=C_DARK, fontName='Helvetica', leading=13)
ST_TOC1       = make_style('TOC1',         fontSize=12, textColor=C_DARK, fontName='Helvetica-Bold', leading=20, spaceBefore=4)
ST_TOC2       = make_style('TOC2',         fontSize=10, textColor=C_DARK, fontName='Helvetica', leading=16, leftIndent=18)

# ─────────────────────────────────────────────
# PHONE MOCKUP GENERATOR
# ─────────────────────────────────────────────

def draw_phone_mockup(screen_name, elements, accent=C_TOOLBAR):
    """
    Draw a realistic phone mockup image for a given screen.
    elements: list of dicts {type, label, description, color_hint}
    Returns: PIL Image
    """
    W, H = 360, 700
    img = PILImage.new('RGBA', (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Phone body (dark rounded rect)
    phone_color = (26, 26, 46)
    border_color = (60, 60, 80)
    d.rounded_rectangle([0, 0, W-1, H-1], radius=45, fill=phone_color, outline=border_color, width=3)

    # Side buttons
    btn_color = (50, 50, 70)
    d.rounded_rectangle([W-6, 140, W-2, 200], radius=3, fill=btn_color)  # power
    d.rounded_rectangle([W-6, 220, W-2, 260], radius=3, fill=btn_color)  # vol+
    d.rounded_rectangle([2, 160, 6, 210],      radius=3, fill=btn_color)  # vol-

    # Camera notch
    d.ellipse([W//2-20, 14, W//2+20, 40], fill=(10,10,20))
    d.ellipse([W//2-6, 20, W//2+6, 34], fill=(40,40,60))

    # Screen area
    sx, sy, ex, ey = 18, 50, W-18, H-50
    d.rounded_rectangle([sx, sy, ex, ey], radius=4, fill=(248, 249, 250))

    # Status bar
    r_val, g_val, b_val = accent.red, accent.green, accent.blue
    sb_col = (int(r_val*255), int(g_val*255), int(b_val*255))
    d.rectangle([sx, sy, ex, sy+28], fill=sb_col)
    # Status bar text (time + icons)
    try:
        font_small = ImageFont.truetype("arial.ttf", 11)
        font_med   = ImageFont.truetype("arial.ttf", 13)
        font_large = ImageFont.truetype("arial.ttf", 15)
        font_bold  = ImageFont.truetype("arialbd.ttf", 13)
        font_code  = ImageFont.truetype("cour.ttf", 9)
    except:
        font_small = ImageFont.load_default()
        font_med   = font_small
        font_large = font_small
        font_bold  = font_small
        font_code  = font_small

    d.text((sx+8, sy+8),  "9:41",    fill='white', font=font_small)
    d.text((ex-50, sy+8), "●●● ⚡",  fill='white', font=font_small)

    # Toolbar
    toolbar_col = sb_col
    d.rectangle([sx, sy+28, ex, sy+82], fill=toolbar_col)
    # Back arrow
    d.polygon([(sx+14, sy+55), (sx+22, sy+47), (sx+22, sy+63)], fill='white')
    d.text((sx+32, sy+47), screen_name, fill='white', font=font_bold)

    # Content area – draw elements
    content_y = sy + 90
    content_x = sx + 12
    content_w = ex - sx - 24

    y_pos = content_y
    MAX_Y = ey - 60

    for elem in elements:
        etype = elem.get('type', 'view').lower()
        label = elem.get('label', '')
        hint  = elem.get('hint', label)
        ecol  = elem.get('color', None)

        if y_pos > MAX_Y:
            break

        if etype == 'toolbar':
            bar_col = ((int((ecol or accent).red*255), int((ecol or accent).green*255), int((ecol or accent).blue*255)) if ecol else sb_col)
            d.rectangle([sx, y_pos-6, ex, y_pos+30], fill=bar_col)
            d.text((content_x, y_pos+6), label, fill='white', font=font_med)
            y_pos += 40

        elif etype in ('edittext', 'textinputlayout', 'textfield'):
            d.rounded_rectangle([content_x, y_pos, content_x+content_w, y_pos+36],
                                  radius=4, fill='white', outline=(180,190,210), width=1)
            d.text((content_x+10, y_pos+10), hint, fill=(150,160,180), font=font_small)
            y_pos += 44

        elif etype == 'button':
            btn_c = ((int((ecol or C_PRIMARY).red*255), int((ecol or C_PRIMARY).green*255), int((ecol or C_PRIMARY).blue*255)) if ecol else (21,101,192))
            d.rounded_rectangle([content_x, y_pos, content_x+content_w, y_pos+40],
                                  radius=8, fill=btn_c)
            tw = d.textlength(label, font=font_med)
            d.text((content_x + (content_w - tw)//2, y_pos+12), label, fill='white', font=font_med)
            y_pos += 52

        elif etype == 'outlinebutton':
            btn_c = ((int((ecol or C_PRIMARY).red*255), int((ecol or C_PRIMARY).green*255), int((ecol or C_PRIMARY).blue*255)) if ecol else (21,101,192))
            d.rounded_rectangle([content_x, y_pos, content_x+content_w, y_pos+38],
                                  radius=8, fill='white', outline=btn_c, width=2)
            tw = d.textlength(label, font=font_med)
            d.text((content_x + (content_w - tw)//2, y_pos+11), label, fill=btn_c, font=font_med)
            y_pos += 50

        elif etype == 'card':
            d.rounded_rectangle([content_x, y_pos, content_x+content_w, y_pos+72],
                                  radius=10, fill='white', outline=(220,225,235), width=1)
            # Company circle avatar
            d.ellipse([content_x+8, y_pos+12, content_x+48, y_pos+52], fill=(200,220,255))
            d.text((content_x+18, y_pos+24), 'Co', fill=(21,101,192), font=font_bold)
            d.text((content_x+58, y_pos+12), label, fill=(30,30,50), font=font_bold)
            d.text((content_x+58, y_pos+30), hint, fill=(100,110,130), font=font_small)
            # Salary badge
            d.rounded_rectangle([content_x+content_w-80, y_pos+44, content_x+content_w-8, y_pos+62],
                                  radius=10, fill=(232,245,232))
            d.text((content_x+content_w-74, y_pos+48), '$1500/mo', fill=(30,120,30), font=font_small)
            y_pos += 82

        elif etype == 'chip':
            chips = label.split(',')
            cx = content_x
            for chip in chips:
                chip = chip.strip()
                tw = int(d.textlength(chip, font=font_small)) + 16
                if cx + tw > content_x + content_w:
                    break
                chip_c = ((int((ecol or C_PRIMARY).red*255), int((ecol or C_PRIMARY).green*255), int((ecol or C_PRIMARY).blue*255)) if ecol else (21,101,192))
                d.rounded_rectangle([cx, y_pos, cx+tw, y_pos+26], radius=13, fill=chip_c)
                d.text((cx+8, y_pos+7), chip, fill='white', font=font_small)
                cx += tw + 8
            y_pos += 36

        elif etype == 'searchbar':
            d.rounded_rectangle([content_x, y_pos, content_x+content_w, y_pos+38],
                                  radius=20, fill=(240,242,245), outline=(210,215,225), width=1)
            d.text((content_x+14, y_pos+12), '🔍  ' + hint, fill=(150,155,165), font=font_med)
            y_pos += 48

        elif etype == 'avatar':
            av_c = ((int((ecol or C_PRIMARY).red*255), int((ecol or C_PRIMARY).green*255), int((ecol or C_PRIMARY).blue*255)) if ecol else (21,101,192))
            cx_ = sx + (ex-sx)//2
            d.ellipse([cx_-38, y_pos, cx_+38, y_pos+76], fill=av_c)
            d.text((cx_-12, y_pos+22), label[:2].upper(), fill='white', font=font_large)
            y_pos += 86

        elif etype == 'textview':
            d.text((content_x, y_pos), label, fill=(30,35,50), font=font_med)
            y_pos += 22

        elif etype == 'textview_small':
            d.text((content_x, y_pos), label, fill=(100,110,130), font=font_small)
            y_pos += 18

        elif etype == 'divider':
            d.line([content_x, y_pos+6, content_x+content_w, y_pos+6], fill=(220,225,235), width=1)
            y_pos += 18

        elif etype == 'switch':
            d.text((content_x, y_pos+3), label, fill=(40,45,60), font=font_med)
            sw_x = content_x + content_w - 50
            sw_on = elem.get('on', False)
            sw_c = (21,101,192) if sw_on else (180,185,195)
            d.rounded_rectangle([sw_x, y_pos, sw_x+44, y_pos+22], radius=11, fill=sw_c)
            kx = sw_x+24 if sw_on else sw_x+2
            d.ellipse([kx, y_pos+2, kx+18, y_pos+20], fill='white')
            y_pos += 32

        elif etype == 'message_sent':
            bw = min(int(content_w*0.65), 200)
            bx = content_x + content_w - bw
            d.rounded_rectangle([bx, y_pos, bx+bw, y_pos+34],
                                  radius=12, fill=(21,101,192))
            d.text((bx+10, y_pos+10), label[:28], fill='white', font=font_small)
            y_pos += 44

        elif etype == 'message_recv':
            bw = min(int(content_w*0.65), 200)
            d.rounded_rectangle([content_x, y_pos, content_x+bw, y_pos+34],
                                  radius=12, fill=(235,237,242))
            d.text((content_x+10, y_pos+10), label[:28], fill=(30,35,50), font=font_small)
            y_pos += 44

        elif etype == 'chat_input':
            d.rounded_rectangle([content_x, y_pos, content_x+content_w-44, y_pos+38],
                                  radius=20, fill='white', outline=(210,215,225), width=1)
            d.text((content_x+12, y_pos+12), hint, fill=(160,165,175), font=font_small)
            d.ellipse([content_x+content_w-40, y_pos+2, content_x+content_w-4, y_pos+36],
                       fill=(21,101,192))
            y_pos += 48

        elif etype == 'call_button':
            btn_c2 = (200,20,20) if label == 'End Call' else (21,101,192)
            cx2 = content_x + (content_w - 56)//2
            d.ellipse([cx2, y_pos, cx2+56, y_pos+56], fill=btn_c2)
            d.text((cx2+14, y_pos+18), label[:4], fill='white', font=font_small)
            y_pos += 70

        elif etype == 'stat_row':
            stats = label.split(',')
            sw2 = content_w // len(stats)
            for i, stat in enumerate(stats):
                parts = stat.strip().split(':')
                val = parts[0].strip()
                lbl = parts[1].strip() if len(parts) > 1 else ''
                bx2 = content_x + i*sw2
                d.text((bx2 + sw2//2 - 10, y_pos), val, fill=(21,101,192), font=font_bold)
                d.text((bx2 + sw2//2 - 15, y_pos+18), lbl, fill=(100,110,130), font=font_small)
            y_pos += 44

        elif etype == 'bottom_nav':
            d.rectangle([sx, ey-44, ex, ey], fill=(250,251,253))
            d.line([sx, ey-44, ex, ey-44], fill=(220,225,235), width=1)
            items = label.split(',')
            iw = (ex - sx) // len(items)
            for i, item in enumerate(items):
                item = item.strip()
                ix = sx + i*iw + iw//2
                is_sel = i == elem.get('selected', 0)
                ic = (21,101,192) if is_sel else (130,135,145)
                d.ellipse([ix-10, ey-38, ix+10, ey-18], fill=ic if is_sel else (230,232,236))
                d.text((ix-len(item)*3, ey-14), item[:6], fill=ic, font=font_small)

        elif etype == 'section_header':
            d.rectangle([sx, y_pos-4, ex, y_pos+24], fill=(240,243,248))
            d.text((content_x, y_pos+4), label.upper(), fill=(80,90,110), font=font_small)
            y_pos += 34

        elif etype == 'list_item':
            d.line([content_x, y_pos+32, content_x+content_w, y_pos+32], fill=(225,228,235), width=1)
            d.text((content_x, y_pos+10), label, fill=(30,35,50), font=font_med)
            d.text((content_x, y_pos+26), hint, fill=(130,135,145), font=font_small)
            # Arrow
            ax = content_x + content_w - 10
            d.polygon([(ax-6, y_pos+14), (ax, y_pos+21), (ax-6, y_pos+28)], fill=(180,185,195))
            y_pos += 42

        elif etype == 'spacer':
            y_pos += elem.get('height', 12)

        elif etype == 'logo':
            # Centered logo circle
            cx3 = sx + (ex-sx)//2
            d.ellipse([cx3-42, y_pos, cx3+42, y_pos+84], fill=(21,101,192))
            d.text((cx3-28, y_pos+22), 'Job', fill='white', font=font_bold)
            d.text((cx3-26, y_pos+44), 'Linker', fill=(180,210,255), font=font_small)
            y_pos += 98

        elif etype == 'progress':
            steps = elem.get('steps', 3)
            cur   = elem.get('current', 1)
            sw3 = content_w // steps
            for i in range(steps):
                bx3 = content_x + i*sw3 + 4
                c3 = (21,101,192) if i < cur else (200,210,225)
                d.rounded_rectangle([bx3, y_pos, bx3+sw3-8, y_pos+8], radius=4, fill=c3)
            y_pos += 22

        elif etype == 'dropdown':
            d.rounded_rectangle([content_x, y_pos, content_x+content_w, y_pos+36],
                                  radius=4, fill='white', outline=(180,190,210), width=1)
            d.text((content_x+10, y_pos+10), hint, fill=(100,110,130), font=font_small)
            # Dropdown arrow
            ax2 = content_x + content_w - 20
            d.polygon([(ax2-6, y_pos+14), (ax2+6, y_pos+14), (ax2, y_pos+24)], fill=(130,140,160))
            y_pos += 44

        elif etype == 'skill_chip':
            skills = label.split(',')
            cx4 = content_x
            line_h = y_pos
            for sk in skills:
                sk = sk.strip()
                tw2 = int(d.textlength(sk, font=font_small)) + 20
                if cx4 + tw2 > content_x + content_w:
                    cx4 = content_x
                    line_h += 32
                if line_h > MAX_Y: break
                d.rounded_rectangle([cx4, line_h, cx4+tw2, line_h+24], radius=12,
                                      fill=(232,240,255), outline=(180,200,240), width=1)
                d.text((cx4+10, line_h+6), sk, fill=(21,101,192), font=font_small)
                cx4 += tw2 + 6
            y_pos = line_h + 34

        else:
            # Generic view block
            d.rounded_rectangle([content_x, y_pos, content_x+content_w, y_pos+30],
                                  radius=4, fill=(245,246,248), outline=(210,215,225), width=1)
            d.text((content_x+10, y_pos+8), label, fill=(60,70,90), font=font_small)
            y_pos += 38

    # Home indicator
    ind_cx = sx + (ex-sx)//2
    d.rounded_rectangle([ind_cx-30, H-30, ind_cx+30, H-22], radius=4, fill=(100,105,120))

    # Convert to RGB for JPEG saving
    rgb_img = PILImage.new('RGB', img.size, 'white')
    rgb_img.paste(img, mask=img.split()[3])
    return rgb_img


def phone_image_flowable(screen_name, elements, width=7*cm, accent=C_TOOLBAR):
    """Return a ReportLab Image flowable of a phone mockup."""
    pil_img = draw_phone_mockup(screen_name, elements, accent=accent)
    buf = io.BytesIO()
    pil_img.save(buf, format='PNG', dpi=(150,150))
    buf.seek(0)
    aspect = pil_img.height / pil_img.width
    return RLImage(buf, width=width, height=width*aspect)


# ─────────────────────────────────────────────
# SCREEN DEFINITIONS
# ─────────────────────────────────────────────

SCREENS = {
    "Splash Screen": {
        "activity": "SplashActivity.java",
        "description": (
            "The Splash Screen is the first screen displayed when the app launches. "
            "It shows the JobLinker logo with a scale and fade-in animation for 2 seconds, "
            "then automatically navigates to MainActivity if the user is already logged in, "
            "or to LoginActivity if they are not."
        ),
        "elements": [
            {"type": "spacer", "height": 30},
            {"type": "logo",   "label": "JobLinker"},
            {"type": "spacer", "height": 20},
            {"type": "textview",       "label": "JobLinker"},
            {"type": "textview_small", "label": "Connecting Employers & Job Seekers"},
            {"type": "spacer", "height": 60},
            {"type": "textview_small", "label": "Powered by Firebase  •  Agora"},
        ],
        "ui_elements": [
            ("ImageView – App Logo",          "Displays the JobLinker logo with scale + fade animation. Centered on screen."),
            ("TextView – App Name",           "Shows 'JobLinker' in large bold white text below the logo."),
            ("TextView – Tagline",            "Subtitle: 'Connecting Employers & Job Seekers' in lighter weight."),
            ("Handler.postDelayed (2000ms)",  "Starts a 2-second timer; on finish, checks SharedPreferences for login status and navigates accordingly."),
            ("Background",                    "Gradient background from C_PRIMARY to C_DARK using a GradientDrawable."),
        ]
    },

    "Login Screen": {
        "activity": "LoginActivity.java",
        "description": (
            "The Login Screen provides two login methods accessible via tabs: "
            "Email/Password and Phone Number. A ViewPager2 with a TabLayout enables smooth "
            "switching between the two methods. A 'Sign Up' link navigates to RegisterActivity."
        ),
        "elements": [
            {"type": "spacer", "height": 10},
            {"type": "logo",   "label": "JL"},
            {"type": "textview",       "label": "Welcome Back"},
            {"type": "textview_small", "label": "Sign in to continue"},
            {"type": "chip", "label": "Email Login,Phone Login"},
            {"type": "edittext", "hint": "Email Address"},
            {"type": "edittext", "hint": "Password"},
            {"type": "textview_small", "label": "Forgot Password?"},
            {"type": "button", "label": "Sign In"},
            {"type": "divider"},
            {"type": "outlinebutton", "label": "Continue with Google", "color": HexColor('#DB4437')},
            {"type": "textview_small", "label": "Don't have an account? Sign Up"},
        ],
        "ui_elements": [
            ("ImageView – Logo",               "App logo at the top for branding."),
            ("TabLayout + ViewPager2",         "Two tabs: 'Email Login' and 'Phone Login'. Hosts LoginEmailFragment and LoginPhoneFragment."),
            ("TextInputLayout – Email",        "Email address input with error validation."),
            ("TextInputLayout – Password",     "Password input with show/hide toggle using PasswordTransformationMethod."),
            ("TextView – Forgot Password",     "Clickable text that triggers Firebase sendPasswordResetEmail()."),
            ("Button – Sign In",               "Calls FirebaseAuth.signInWithEmailAndPassword() on click."),
            ("Button – Google Sign-In",        "Launches Google Sign-In intent via GoogleSignInClient."),
            ("TextView – Sign Up",             "Navigates to RegisterActivity when clicked."),
            ("ProgressBar",                    "Shown during authentication; hidden when idle."),
        ]
    },

    "Register – Step 1": {
        "activity": "RegisterActivity.java / RegisterStep1Fragment.java",
        "description": (
            "The first step of the 3-step registration flow. "
            "The user enters their full name, email address, phone number, and password. "
            "They also select their account role: Job Seeker or Employer. "
            "A progress indicator at the top shows 'Step 1 of 3'."
        ),
        "elements": [
            {"type": "progress", "steps": 3, "current": 1},
            {"type": "textview",  "label": "Create Account"},
            {"type": "textview_small", "label": "Step 1 of 3 – Basic Information"},
            {"type": "edittext",  "hint": "Full Name"},
            {"type": "edittext",  "hint": "Email Address"},
            {"type": "edittext",  "hint": "Phone Number"},
            {"type": "edittext",  "hint": "Password"},
            {"type": "chip", "label": "Job Seeker,Employer"},
            {"type": "button", "label": "Next →"},
        ],
        "ui_elements": [
            ("Step Indicator (3 dots)",        "Visual progress bar showing current registration step (1/3)."),
            ("TextInputLayout – Full Name",    "Validated: must not be empty, minimum 2 characters."),
            ("TextInputLayout – Email",        "Validated with Android Patterns.EMAIL_ADDRESS."),
            ("TextInputLayout – Phone",        "Validated: must be 9–15 digits with optional + prefix."),
            ("TextInputLayout – Password",     "Validated: minimum 6 characters. Show/hide toggle."),
            ("ToggleButton Group – Role",      "Job Seeker or Employer selection. Determines which features are shown post-registration."),
            ("Button – Next",                  "Validates all fields then calls ViewPager2.setCurrentItem(1) to go to Step 2."),
        ]
    },

    "Register – Step 2": {
        "activity": "RegisterStep2Fragment.java",
        "description": (
            "The second registration step collects professional information. "
            "For Job Seekers: years of experience, current city, and a short bio. "
            "For Employers: company name, company website, and industry. "
            "The fields shown are dynamic based on the role selected in Step 1."
        ),
        "elements": [
            {"type": "progress", "steps": 3, "current": 2},
            {"type": "textview",  "label": "Professional Info"},
            {"type": "textview_small", "label": "Step 2 of 3 – Work Details"},
            {"type": "dropdown", "hint": "Years of Experience"},
            {"type": "edittext",  "hint": "Current City"},
            {"type": "edittext",  "hint": "Company Name (Employers)"},
            {"type": "edittext",  "hint": "Short Bio"},
            {"type": "button", "label": "Next →"},
        ],
        "ui_elements": [
            ("Step Indicator",                 "Shows step 2 of 3 highlighted."),
            ("AutoCompleteTextView – Experience", "Dropdown for years of experience (e.g., 1–2 years, 3–5 years, etc.)."),
            ("TextInputLayout – City",         "User's current city for location-based job filtering."),
            ("TextInputLayout – Company Name", "Shown only for Employer role. Required if employer."),
            ("TextInputLayout – Website",      "Optional company website URL for employers."),
            ("TextInputLayout – Bio",          "Short biography (max 200 chars). Multi-line input."),
            ("Button – Next",                  "Moves to Step 3 (confirmation + Firebase user creation)."),
            ("Button – Back",                  "Returns to Step 1 without losing entered data."),
        ]
    },

    "Main – Jobs Screen": {
        "activity": "JobsFragment.java",
        "description": (
            "The main browsing screen for job listings. Users can search by keyword, "
            "apply quick-filter chips (All, Full-time, Part-time, Remote, etc.), or open "
            "the advanced filter dialog. Results update in real-time from Firestore. "
            "Employers see a FAB to post a new job."
        ),
        "elements": [
            {"type": "searchbar", "hint": "Search jobs..."},
            {"type": "chip", "label": "All,Full-time,Part-time,Remote"},
            {"type": "textview_small", "label": "247 jobs found"},
            {"type": "card", "label": "UI/UX Designer", "hint": "Google • Tel Aviv"},
            {"type": "card", "label": "Backend Developer", "hint": "Microsoft • Remote"},
            {"type": "card", "label": "Product Manager", "hint": "Meta • Haifa"},
            {"type": "bottom_nav", "label": "Jobs,Chats,Profile", "selected": 0},
        ],
        "ui_elements": [
            ("SearchView / TextInputLayout",   "Search bar at top. Uses 300ms debounce to avoid excessive Firestore queries while typing."),
            ("ImageButton – Filter",           "Opens FilterActivity for advanced filters (category, salary range, location, sort)."),
            ("ChipGroup – Quick Filters",      "Chips: All / Full-time / Part-time / Remote / Contract / Internship. Single-select chips that filter job type."),
            ("TextView – Result Count",        "Shows 'X jobs found' dynamically based on active filters."),
            ("RecyclerView – Job List",        "Displays job cards using JobAdapter. Each card shows: company logo, title, company name, location, salary range, bookmark icon."),
            ("FloatingActionButton – Post Job","Visible only for Employer role. Opens PostJobActivity."),
            ("BottomNavigationView",           "Three tabs: Jobs (home), Chats (messages icon with badge), Profile."),
            ("SwipeRefreshLayout",             "Pull-to-refresh re-fetches jobs from Firestore."),
        ]
    },

    "Job Details": {
        "activity": "JobDetailsActivity.java",
        "description": (
            "Detailed view of a single job posting. Shows company logo, job title, "
            "employment type, location, salary range, full description, required skills, "
            "and deadline. Job seekers can apply (with cover letter), save the job, "
            "or contact the employer directly. View count is tracked."
        ),
        "elements": [
            {"type": "textview", "label": "UI/UX Designer"},
            {"type": "textview_small", "label": "Google  •  Tel Aviv  •  Full-time"},
            {"type": "textview_small", "label": "$3,000 – $5,000 / month  •  Deadline: June 30"},
            {"type": "divider"},
            {"type": "textview", "label": "Job Description"},
            {"type": "textview_small", "label": "We are looking for a creative UI/UX..."},
            {"type": "skill_chip", "label": "Figma,Adobe XD,Prototyping,Wireframing"},
            {"type": "divider"},
            {"type": "textview", "label": "About the Employer"},
            {"type": "card", "label": "Ahmed (Employer)", "hint": "Google  •  5 yrs exp"},
            {"type": "button", "label": "Apply Now"},
            {"type": "outlinebutton", "label": "Contact Employer"},
        ],
        "ui_elements": [
            ("ImageView – Company Logo",       "Loaded from Firebase Storage using Glide. Shown in a CircleImageView."),
            ("TextView – Job Title",           "Large bold title at the top."),
            ("Chip – Employment Type",         "Badge showing Full-time / Part-time / Remote / etc."),
            ("TextView – Location",            "City and country with a location pin icon."),
            ("TextView – Salary Range",        "Min–Max salary with currency symbol."),
            ("TextView – Description",         "Full multi-line job description. Expandable on 'Read More' tap."),
            ("ChipGroup – Required Skills",    "Dynamic skill chips populated from job.jobSkills list."),
            ("CardView – Employer Info",       "Shows employer avatar, name, company, and experience."),
            ("Button – Apply Now",             "Opens a BottomSheetDialog with a cover letter text field. On submit, writes to 'applications' collection."),
            ("Button – Contact",               "Opens ChatActivity with the employer."),
            ("ImageButton – Bookmark",         "Saves/unsaves the job to 'savedJobs' collection. Toggles icon fill."),
            ("MenuItem – Share",               "Opens Android share sheet with job details as plain text."),
            ("MenuItem – Report",              "Opens a dialog with report reasons; writes to 'reports' collection."),
        ]
    },

    "Post Job": {
        "activity": "PostJobActivity.java",
        "description": (
            "Employers use this screen to post a new job vacancy. "
            "Required fields include job title, company name, category, employment type, "
            "location, salary range, skills, and deadline. "
            "An 'Urgent' toggle marks high-priority listings."
        ),
        "elements": [
            {"type": "textview",  "label": "Post a New Job"},
            {"type": "edittext",  "hint": "Job Title *"},
            {"type": "edittext",  "hint": "Company Name *"},
            {"type": "dropdown",  "hint": "Category (Technology, Finance…)"},
            {"type": "dropdown",  "hint": "Employment Type"},
            {"type": "edittext",  "hint": "Location / City"},
            {"type": "edittext",  "hint": "Min Salary"},
            {"type": "edittext",  "hint": "Max Salary"},
            {"type": "skill_chip","label": "Add Skills,Java,Firebase"},
            {"type": "button",    "label": "Pick Deadline Date"},
            {"type": "switch",    "label": "Mark as Urgent", "on": False},
            {"type": "button",    "label": "Post Job"},
        ],
        "ui_elements": [
            ("TextInputLayout – Job Title",    "Required. Validated: not empty."),
            ("TextInputLayout – Company Name", "Required. Pre-filled from the employer's profile if available."),
            ("AutoCompleteTextView – Category","Dropdown populated from R.array.job_categories (Technology, Finance, Healthcare, etc.)."),
            ("AutoCompleteTextView – Type",    "Employment type: Full-time, Part-time, Remote, Contract, Internship."),
            ("TextInputLayout – Location",     "City or 'Remote'."),
            ("TextInputLayout – Min/Max Salary","Numeric fields for salary range. Currency selector next to them."),
            ("ChipGroup – Skills",             "User types a skill and presses Add; a chip with ✕ icon is created dynamically."),
            ("Button – Pick Deadline",         "Opens DatePickerDialog. Minimum date = today."),
            ("SwitchCompat – Urgent",          "Marks job as urgent; shown with a red badge in job cards."),
            ("Button – Post Job",              "Validates all required fields, then writes a new Job document to Firestore. Shows ProgressBar overlay while saving."),
        ]
    },

    "Chats List": {
        "activity": "ChatsFragment.java",
        "description": (
            "Displays all the user's conversations in a list. "
            "Each item shows the other user's avatar, name, online status, "
            "last message preview, timestamp, and unread message count badge. "
            "A search bar filters conversations by contact name."
        ),
        "elements": [
            {"type": "textview",  "label": "Messages"},
            {"type": "searchbar", "hint": "Search conversations..."},
            {"type": "list_item", "label": "Ahmed (Employer)",  "hint": "Thanks for applying! We'll..."},
            {"type": "list_item", "label": "Sara (Job Seeker)", "hint": "Can we schedule an interview..."},
            {"type": "list_item", "label": "Tech Corp HR",      "hint": "Your application has been..."},
            {"type": "list_item", "label": "Majd (Recruiter)",  "hint": "Please send your portfolio"},
            {"type": "bottom_nav", "label": "Jobs,Chats,Profile", "selected": 1},
        ],
        "ui_elements": [
            ("SearchView",                     "Filters conversation list by the other user's name in real-time."),
            ("RecyclerView – Conversation List","Uses ConversationAdapter. Each item is item_conversation.xml."),
            ("CircleImageView – Avatar",       "Other user's profile photo. Default shown if none uploaded."),
            ("TextView – Online Indicator",    "Green dot shown when isOnline == true in Firestore."),
            ("TextView – Last Message",        "Preview of the last message text (truncated to ~40 chars)."),
            ("TextView – Timestamp",           "Relative time: 'just now', '5 min ago', 'Yesterday', or date."),
            ("Badge – Unread Count",           "Red bubble with unread message count. Hidden when 0."),
            ("BottomNavigationView Badge",     "Total unread messages shown as a badge on the Chats tab icon."),
            ("Empty State View",               "Shown when no conversations exist yet."),
        ]
    },

    "Chat Screen": {
        "activity": "ChatActivity.java",
        "description": (
            "Full-featured real-time messaging screen between two users. "
            "Supports text, images, voice recordings, documents, location, and contact messages. "
            "Shows typing indicators, read receipts (single/double checkmarks), "
            "and online/offline status in the toolbar."
        ),
        "elements": [
            {"type": "message_recv", "label": "Hello! Is the position still open?"},
            {"type": "message_sent", "label": "Yes, please send your CV."},
            {"type": "message_recv", "label": "I'll send it right now!"},
            {"type": "message_sent", "label": "Great, looking forward to it."},
            {"type": "textview_small", "label": "Ahmed is typing..."},
            {"type": "spacer", "height": 8},
            {"type": "chat_input", "hint": "Type a message..."},
        ],
        "ui_elements": [
            ("RecyclerView – Messages",        "Uses MessageAdapter with DiffUtil for efficient updates. Scrolls to bottom on new message."),
            ("item_message_sent.xml",          "Outgoing message bubble (blue, right-aligned). Shows timestamp + read receipt checkmarks."),
            ("item_message_received.xml",      "Incoming message bubble (gray, left-aligned). Shows sender avatar + timestamp."),
            ("TextInputLayout – Message Input","Message text field with auto-resize and 300-char limit."),
            ("ImageButton – Send",             "Sends a text message to Firestore 'messages' collection."),
            ("ImageButton – Attach",           "Opens attachment tray: Camera, Gallery, Document, Location, Contact."),
            ("ImageButton – Voice Record",     "Hold to record audio via MediaRecorder. Release to send. Shows recording timer."),
            ("TextView – Typing Indicator",    "Updates Firestore 'isTyping' field; other user's client shows animated dots."),
            ("Toolbar – Online Status",        "Shows online/offline dot. Updates via Firestore real-time listener."),
        ]
    },

    "Voice Call": {
        "activity": "CallActivity.java",
        "description": (
            "The call interface for voice and video calls using the Agora RTC SDK. "
            "Shows the remote user's avatar and name, a live call timer, and control buttons: "
            "Mute, Speaker, End Call, and (for video calls) Camera Toggle. "
            "Call details are stored in Firestore for call history."
        ),
        "elements": [
            {"type": "spacer", "height": 30},
            {"type": "avatar", "label": "AH", "color": HexColor('#1565C0')},
            {"type": "textview",       "label": "Ahmed Al-Hassan"},
            {"type": "textview_small", "label": "Voice Call  •  00:02:34"},
            {"type": "spacer", "height": 20},
            {"type": "call_button", "label": "Mute"},
            {"type": "call_button", "label": "Spkr"},
            {"type": "call_button", "label": "End Call"},
        ],
        "ui_elements": [
            ("CircleImageView – Remote Avatar","Shows the other user's profile photo (or initials placeholder)."),
            ("TextView – Call Name",           "Displays the other user's full name."),
            ("TextView – Call Timer",          "Counts up in HH:MM:SS format using a Handler posted every second."),
            ("ImageButton – Mute",             "Toggles microphone mute. Calls AgoraRtcEngine.muteLocalAudioStream()."),
            ("ImageButton – Speaker",          "Toggles speakerphone. Calls AgoraRtcEngine.setEnableSpeakerphone()."),
            ("ImageButton – End Call",         "Calls AgoraRtcEngine.leaveChannel(), updates call record in Firestore with duration, then finishes the activity."),
            ("ImageButton – Camera Toggle",    "Visible only for video calls. Toggles front/rear camera."),
            ("SurfaceView – Local/Remote Video","Used for video calls. Initialized with AgoraRtcEngine.setupLocalVideo() and setupRemoteVideo()."),
        ]
    },

    "Profile Screen": {
        "activity": "ProfileFragment.java",
        "description": (
            "The user's public profile page. Shows avatar, full name, role badge, "
            "online indicator, contact info (email, phone, city), a short bio, "
            "statistics (jobs posted / applications / connections / rating), "
            "and skills chips for Job Seekers. Buttons for Edit Profile, Settings, and Logout."
        ),
        "elements": [
            {"type": "spacer", "height": 10},
            {"type": "avatar",  "label": "MJ", "color": HexColor('#1565C0')},
            {"type": "textview",        "label": "Majd Ahmed Majdoub"},
            {"type": "chip",            "label": "Job Seeker"},
            {"type": "textview_small",  "label": "📍 Tel Aviv  •  ✉ majd@example.com"},
            {"type": "textview_small",  "label": "Passionate developer seeking opportunities"},
            {"type": "stat_row",        "label": "12:Apps,4.8:Rating,38:Saved"},
            {"type": "divider"},
            {"type": "textview",        "label": "Skills"},
            {"type": "skill_chip",      "label": "Java,Firebase,Android,SQL,Figma"},
            {"type": "divider"},
            {"type": "button",          "label": "Edit Profile"},
            {"type": "outlinebutton",   "label": "Settings"},
            {"type": "bottom_nav",      "label": "Jobs,Chats,Profile", "selected": 2},
        ],
        "ui_elements": [
            ("CircleImageView – Avatar",        "Profile photo with online indicator (green dot overlay)."),
            ("TextView – Name",                 "User's full name in bold."),
            ("Chip – Role Badge",               "Shows 'Job Seeker' or 'Employer' with corresponding icon and color."),
            ("TextView – Bio",                  "Short bio text. Shown only if user has set one."),
            ("GridLayout – Stats",              "3 stats: Applications Sent / Rating / Saved Jobs (job seekers) or Jobs Posted / Applicants / Rating (employers)."),
            ("RecyclerView – My Jobs",          "Shown for Employer role only. Lists all jobs posted by this employer."),
            ("ChipGroup – Skills",              "Shown for Job Seeker role. Displays user.userSkills list as chips."),
            ("Button – Edit Profile",           "Opens EditProfileActivity."),
            ("Button – Settings",               "Opens SettingActivity."),
            ("Button – Logout",                 "Shows LogoutHelper confirmation dialog. Clears SharedPreferences and returns to LoginActivity."),
        ]
    },

    "Edit Profile": {
        "activity": "EditProfileActivity.java",
        "description": (
            "Allows users to update their profile information including avatar, "
            "name, phone, city, country, language, currency, bio, and skills. "
            "The avatar can be changed via camera or gallery. Changes are saved to "
            "Firestore and the avatar image is uploaded to Firebase Storage."
        ),
        "elements": [
            {"type": "avatar",   "label": "MJ"},
            {"type": "textview_small", "label": "Tap to change photo"},
            {"type": "edittext", "hint": "Full Name"},
            {"type": "edittext", "hint": "Phone Number"},
            {"type": "edittext", "hint": "City"},
            {"type": "dropdown", "hint": "Country"},
            {"type": "dropdown", "hint": "Language"},
            {"type": "edittext", "hint": "Short Bio"},
            {"type": "button",   "label": "Save Changes"},
        ],
        "ui_elements": [
            ("CircleImageView + FAB",          "Profile photo with a camera FAB overlay. Tapping opens a BottomSheet: 'Take Photo' or 'Choose from Gallery'."),
            ("TextInputLayout – Name",         "Pre-filled with current name. Validated on save."),
            ("TextInputLayout – Phone",        "Phone number. Validated for correct format."),
            ("TextInputLayout – City",         "Current city. Used in job location matching."),
            ("AutoCompleteTextView – Country", "Dropdown for country selection from R.array.countries."),
            ("AutoCompleteTextView – Language","Preferred language: English, Arabic, Hebrew, etc."),
            ("AutoCompleteTextView – Currency","Preferred currency for salary display: ILS, USD, EUR."),
            ("TextInputLayout – Bio",          "Multi-line, max 200 characters."),
            ("Button – Save Changes",          "Validates, uploads photo if changed (shows ProgressBar), then updates Firestore user document."),
        ]
    },

    "Settings": {
        "activity": "SettingActivity.java",
        "description": (
            "The Settings screen is organized into sections: Account (edit profile, "
            "change password, email verification), Job Tools (saved jobs, my applications), "
            "Preferences (notifications, dark mode, online status), Privacy, and Support. "
            "A Danger Zone section at the bottom offers Delete Account and Logout."
        ),
        "elements": [
            {"type": "section_header", "label": "Account"},
            {"type": "list_item", "label": "Edit Profile",       "hint": "Update your info"},
            {"type": "list_item", "label": "Change Password",    "hint": "Update your password"},
            {"type": "list_item", "label": "Email Verification", "hint": "Verify email address"},
            {"type": "section_header", "label": "Preferences"},
            {"type": "switch", "label": "Push Notifications",    "on": True},
            {"type": "switch", "label": "Job Alerts",            "on": True},
            {"type": "switch", "label": "Dark Mode",             "on": False},
            {"type": "switch", "label": "Show Online Status",    "on": True},
            {"type": "section_header", "label": "Support"},
            {"type": "list_item", "label": "Help & FAQ",         "hint": ""},
            {"type": "list_item", "label": "Terms of Service",   "hint": ""},
            {"type": "list_item", "label": "About JobLinker",    "hint": "v1.0.0"},
        ],
        "ui_elements": [
            ("Section Headers",                "Visually separate setting groups: Account / Job Tools / Preferences / Privacy / Support / Danger Zone."),
            ("TextView – Edit Profile",        "Navigates to EditProfileActivity."),
            ("TextView – Change Password",     "Opens dialog_change_password.xml: current password + new password fields."),
            ("TextView – Email Verification",  "Sends Firebase verification email. Disabled if already verified."),
            ("TextView – Saved Jobs",          "Navigates to SavedJobsActivity (Job Seeker only)."),
            ("SwitchCompat – Notifications",   "Toggle push notifications. Stored in SharedPreferences."),
            ("SwitchCompat – Job Alerts",      "Toggle job-match notifications from FCM."),
            ("SwitchCompat – Dark Mode",       "Calls AppCompatDelegate.setDefaultNightMode(). Instantly applies."),
            ("SwitchCompat – Online Status",   "When off, sets user.isOnline = false in Firestore."),
            ("TextView – Delete Account",      "Shows delete_confirmation_dialog.xml. On confirm, calls FirebaseAuth.deleteUser() and removes Firestore data."),
            ("TextView – Logout",              "Calls LogoutHelper.showLogoutDialog() for confirmation."),
        ]
    },

    "Filter Jobs": {
        "activity": "FilterActivity.java",
        "description": (
            "Advanced filter screen launched from the Jobs screen. "
            "Users can filter by job type (dropdown), category (dropdown), "
            "location (text input), minimum and maximum salary, and sort order. "
            "Results are passed back to JobsFragment via ActivityResultLauncher."
        ),
        "elements": [
            {"type": "textview",  "label": "Filter Jobs"},
            {"type": "dropdown",  "hint": "Job Type (All, Full-time…)"},
            {"type": "dropdown",  "hint": "Category (Technology…)"},
            {"type": "edittext",  "hint": "Location / City"},
            {"type": "edittext",  "hint": "Min Salary ($)"},
            {"type": "edittext",  "hint": "Max Salary ($)"},
            {"type": "dropdown",  "hint": "Sort: Newest First"},
            {"type": "button",    "label": "Apply Filters"},
            {"type": "outlinebutton", "label": "Clear All"},
        ],
        "ui_elements": [
            ("AutoCompleteTextView – Job Type","Filter by All / Full-time / Part-time / Remote / Contract / Internship."),
            ("AutoCompleteTextView – Category","Filter by job category from R.array.job_categories."),
            ("TextInputLayout – Location",     "Free-text location filter (case-insensitive substring match)."),
            ("TextInputLayout – Min Salary",   "Numeric filter. Jobs with salary >= this value are shown."),
            ("TextInputLayout – Max Salary",   "Numeric filter. Jobs with salary <= this value are shown."),
            ("AutoCompleteTextView – Sort",    "Sort order: Newest First or Oldest First."),
            ("Button – Apply",                 "Builds a FilterState object and returns it to JobsFragment via setResult(RESULT_OK, intent)."),
            ("Button – Clear",                 "Resets all filter fields to default values."),
        ]
    },
}

# ── SavedJobsActivity screen definition ──────────────────────────
SCREENS["Saved Jobs"] = {
    "activity": "SavedJobsActivity.java",
    "description": (
        "Displays all jobs the user has bookmarked using the save (bookmark) icon in job cards "
        "or on the Job Details screen. Each item shows the job title, company, location, "
        "and salary. Tapping a card opens JobDetailsActivity. An empty-state view is shown "
        "when no jobs have been saved yet."
    ),
    "elements": [
        {"type": "textview",       "label": "Saved Jobs"},
        {"type": "textview_small", "label": "12 jobs saved"},
        {"type": "card", "label": "UI/UX Designer",    "hint": "Google  •  Tel Aviv"},
        {"type": "card", "label": "Android Developer", "hint": "Microsoft  •  Remote"},
        {"type": "card", "label": "Product Manager",   "hint": "Meta  •  Haifa"},
        {"type": "card", "label": "DevOps Engineer",   "hint": "Amazon  •  Jerusalem"},
        {"type": "bottom_nav", "label": "Jobs,Chats,Profile", "selected": 2},
    ],
    "ui_elements": [
        ("Toolbar – 'Saved Jobs'",     "Back arrow + title. Standard AppCompat toolbar."),
        ("TextView – Count",           "Shows 'X jobs saved' subtitle below the toolbar."),
        ("RecyclerView – Job List",    "Uses the same JobAdapter as the main Jobs screen. Displays bookmarked jobs fetched from Firestore 'savedJobs' collection joined with 'jobs'."),
        ("item_job.xml – Job Card",    "Company logo (Glide), job title, company name, location, salary range, bookmark icon (pre-filled)."),
        ("Empty State View",           "Shown when the savedJobs list is empty. ImageView + 'No saved jobs yet' message + 'Browse Jobs' button."),
        ("JobAdapter.OnJobClickListener","Opens JobDetailsActivity when a card is tapped."),
    ]
}

# ─────────────────────────────────────────────
# PDF DOCUMENT CLASS (CUSTOM PAGE TEMPLATES)
# ─────────────────────────────────────────────

class BookDocTemplate(BaseDocTemplate):
    def __init__(self, filename, **kwargs):
        super().__init__(filename, **kwargs)
        self.student_name = "Majd Ahmed Majdoub"
        self.school_name  = "Al-Bayan Comprehensive School"
        self.github       = "github.com/Majd868/JobLinker"

        frame_normal = Frame(
            1.8*cm, 2.2*cm,
            PAGE_W - 3.6*cm, PAGE_H - 4.4*cm,
            id='normal'
        )
        frame_cover = Frame(
            0, 0, PAGE_W, PAGE_H, id='cover',
            leftPadding=0, rightPadding=0, topPadding=0, bottomPadding=0
        )

        self.addPageTemplates([
            PageTemplate(id='Cover', frames=[frame_cover],
                         onPage=self._draw_cover_page),
            PageTemplate(id='Normal', frames=[frame_normal],
                         onPage=self._draw_normal_page),
        ])

    def _draw_normal_page(self, canvas, doc):
        canvas.saveState()
        w, h = PAGE_W, PAGE_H

        # Top bar
        canvas.setFillColor(C_PRIMARY)
        canvas.rect(0, h-1.6*cm, w, 1.6*cm, fill=1, stroke=0)

        # Header text
        canvas.setFillColor(white)
        canvas.setFont('Helvetica-Bold', 9)
        canvas.drawString(1.8*cm, h-0.95*cm, self.school_name)
        canvas.setFont('Helvetica', 9)
        canvas.drawRightString(w-1.8*cm, h-0.95*cm, self.student_name)

        # Blue accent line under header
        canvas.setFillColor(C_ACCENT)
        canvas.rect(0, h-1.8*cm, w, 0.2*cm, fill=1, stroke=0)

        # Footer line
        canvas.setStrokeColor(C_DIVIDER)
        canvas.setLineWidth(0.5)
        canvas.line(1.8*cm, 1.8*cm, w-1.8*cm, 1.8*cm)

        # Footer text
        canvas.setFillColor(HexColor('#78909C'))
        canvas.setFont('Helvetica', 8)
        canvas.drawString(1.8*cm, 1.1*cm,
                          f"JobLinker Project Book  •  Android / Java / Firebase / Agora")
        canvas.drawRightString(w-1.8*cm, 1.1*cm,
                               f"Page {doc.page}")

        # GitHub link
        canvas.setFont('Helvetica', 7.5)
        canvas.setFillColor(C_ACCENT)
        canvas.drawCentredString(w/2, 0.6*cm, self.github)

        canvas.restoreState()

    def _draw_cover_page(self, canvas, doc):
        pass  # Cover is fully custom – drawn in story

    def afterFlowable(self, flowable):
        """Register H1/H2 paragraphs as TOC entries after each flowable is placed."""
        if isinstance(flowable, Paragraph):
            name = flowable.style.name
            text = flowable.getPlainText()
            if not text.strip():
                return
            if name == 'H1':
                self.notify('TOCEntry', (0, text, self.page))
            elif name == 'H2':
                self.notify('TOCEntry', (1, text, self.page))


# ─────────────────────────────────────────────
# HELPER: CODE BLOCK
# ─────────────────────────────────────────────

def code_block(text, max_chars=92):
    """Return Paragraph flowables for a code block (plain safe text, no HTML coloring)."""
    lines = text.splitlines()
    result = []
    for line in lines:
        if len(line) > max_chars:
            line = line[:max_chars] + '  [...]'
        safe = (line
                .replace('&', '&amp;')
                .replace('<', '&lt;')
                .replace('>', '&gt;'))
        result.append(Paragraph(safe, ST_CODE))
    return result


def section_code(title, code_text, language="Java"):
    """Return flowables for a titled source code section."""
    items = []
    items.append(Spacer(1, 6))
    items.append(Paragraph(f"<b>{title}</b>  "
                           f'<font size="8" color="#78909C">({language})</font>', ST_H3))
    items.append(Spacer(1, 3))

    # Code block with background
    code_lines = code_block(code_text)
    # Each line becomes a row in a single-column table so it paginates properly
    av = PAGE_W - 3.6*cm
    rows = [[line] for line in code_lines]
    bg_table = Table(rows, colWidths=[av])
    bg_table.setStyle(TableStyle([
        ('BACKGROUND',   (0,0), (-1,-1), C_CODE_BG),
        ('LEFTPADDING',  (0,0), (-1,-1), 10),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
        ('TOPPADDING',   (0,0), (-1,-1), 0),
        ('BOTTOMPADDING',(0,0), (-1,-1), 0),
        ('LINEABOVE',    (0,0), (-1,0),  0.5, HexColor('#333344')),
        ('LINEBELOW',    (0,-1),(-1,-1), 0.5, HexColor('#333344')),
    ]))
    items.append(bg_table)
    items.append(Spacer(1, 8))
    return items


def make_info_table(rows, col_widths=None):
    """Create a styled two-column info table."""
    if col_widths is None:
        available = PAGE_W - 3.6*cm
        col_widths = [available*0.30, available*0.70]

    table_data = [[
        Paragraph("UI Element", ST_TABLE_HDR),
        Paragraph("Description", ST_TABLE_HDR),
    ]]
    for name, desc in rows:
        table_data.append([
            Paragraph(f"<b>{name}</b>", ST_TABLE_CELL),
            Paragraph(desc, ST_TABLE_CELL),
        ])

    t = Table(table_data, colWidths=col_widths)
    t.setStyle(TableStyle([
        ('BACKGROUND',   (0,0), (-1,0),  C_PRIMARY),
        ('TEXTCOLOR',    (0,0), (-1,0),  white),
        ('ALIGN',        (0,0), (-1,-1), 'LEFT'),
        ('VALIGN',       (0,0), (-1,-1), 'TOP'),
        ('FONTNAME',     (0,0), (-1,0),  'Helvetica-Bold'),
        ('FONTSIZE',     (0,0), (-1,0),  9),
        ('ROWBACKGROUNDS',(0,1),(-1,-1), [C_SURFACE, white]),
        ('GRID',         (0,0), (-1,-1), 0.4, C_DIVIDER),
        ('LEFTPADDING',  (0,0), (-1,-1), 8),
        ('RIGHTPADDING', (0,0), (-1,-1), 8),
        ('TOPPADDING',   (0,0), (-1,-1), 5),
        ('BOTTOMPADDING',(0,0), (-1,-1), 5),
        ('ROUNDEDCORNERS',(0,0),(-1,-1), [4,4,4,4]),
    ]))
    return t


# ─────────────────────────────────────────────
# COVER PAGE
# ─────────────────────────────────────────────

def build_cover():
    """Draw the book cover page using ReportLab canvas operations inside a custom flowable."""
    from reportlab.platypus import Flowable

    class CoverPage(Flowable):
        def __init__(self):
            Flowable.__init__(self)
            self.width  = PAGE_W
            self.height = PAGE_H

        def draw(self):
            c = self.canv
            w, h = PAGE_W, PAGE_H

            # Background gradient simulation (dark)
            c.setFillColor(C_DARK)
            c.rect(0, 0, w, h, fill=1, stroke=0)

            # Blue gradient block top half
            c.setFillColor(C_PRIMARY)
            c.rect(0, h*0.45, w, h*0.55, fill=1, stroke=0)

            # Diagonal accent
            from reportlab.graphics.shapes import Polygon
            c.setFillColor(HexColor('#0D47A1'))
            c.setStrokeColor(HexColor('#0D47A1'))
            path = c.beginPath()
            path.moveTo(0, h*0.45)
            path.lineTo(w, h*0.55)
            path.lineTo(w, h*0.45)
            path.close()
            c.drawPath(path, fill=1, stroke=0)

            # Decorative circles
            c.setFillColor(HexColor('#1976D280'))
            c.circle(w*0.85, h*0.75, 80, fill=1, stroke=0)
            c.setFillColor(HexColor('#0D47A140'))
            c.circle(w*0.15, h*0.6,  60, fill=1, stroke=0)

            # App logo circle
            cx, cy = w/2, h*0.67
            c.setFillColor(white)
            c.circle(cx, cy, 55, fill=1, stroke=0)
            c.setFillColor(C_PRIMARY)
            c.setFont('Helvetica-Bold', 28)
            c.drawCentredString(cx, cy-8, 'JL')
            c.setFont('Helvetica', 9)
            c.setFillColor(C_ACCENT)
            c.drawCentredString(cx, cy-22, 'JobLinker')

            # Title
            c.setFillColor(white)
            c.setFont('Helvetica-Bold', 36)
            c.drawCentredString(cx, h*0.50, 'JobLinker')
            c.setFont('Helvetica', 15)
            c.setFillColor(C_ACCENT)
            c.drawCentredString(cx, h*0.44, 'Project Documentation Book')
            c.setFont('Helvetica', 11)
            c.setFillColor(HexColor('#B0BEC5'))
            c.drawCentredString(cx, h*0.40,
                'Connecting Employers & Job Seekers')

            # Divider line
            c.setStrokeColor(HexColor('#42A5F580'))
            c.setLineWidth(1.5)
            c.line(w*0.2, h*0.37, w*0.8, h*0.37)

            # Info block
            info = [
                ("Student",    "Majd Ahmed Majdoub"),
                ("ID Number",  "217229301"),
                ("School",     "Al-Bayan Comprehensive School"),
                ("Supervisor", "Mr. Iyad Marieh"),
                ("Track",      "Computer Science – Systems Planning"),
                ("Platform",   "Android  •  Java  •  Firebase  •  Agora"),
                ("Date",       "April 16, 2026"),
            ]
            y_start = h*0.34
            for label, value in info:
                c.setFont('Helvetica-Bold', 10)
                c.setFillColor(C_ACCENT)
                c.drawString(w*0.20, y_start, label + ':')
                c.setFont('Helvetica', 10)
                c.setFillColor(white)
                c.drawString(w*0.40, y_start, value)
                y_start -= 0.45*cm

            # Tech badges
            badges = ['Android', 'Java', 'Firebase', 'Agora RTC', 'Material Design']
            bx = w*0.10
            by = h*0.08
            bw = (w*0.80) / len(badges)
            for badge in badges:
                c.setFillColor(HexColor('#1976D2'))
                c.roundRect(bx+2, by, bw-8, 22, 6, fill=1, stroke=0)
                c.setFillColor(white)
                c.setFont('Helvetica-Bold', 8)
                c.drawCentredString(bx + bw/2, by+7, badge)
                bx += bw

            # Bottom
            c.setFillColor(HexColor('#78909C'))
            c.setFont('Helvetica', 8)
            c.drawCentredString(cx, h*0.04, 'github.com/Majd868/JobLinker')

    return [CoverPage(), NextPageTemplate('Normal'), PageBreak()]


# ─────────────────────────────────────────────
# SECTION BUILDERS
# ─────────────────────────────────────────────

def section_divider(title, subtitle=""):
    """A full-page section divider."""
    items = [PageBreak()]
    # Blue banner
    from reportlab.platypus import Flowable

    class SectionBanner(Flowable):
        def __init__(self, t, st):
            Flowable.__init__(self)
            self.t  = t
            self.st = st
            self.width  = PAGE_W - 3.6*cm
            self.height = 4*cm

        def draw(self):
            c = self.canv
            c.setFillColor(C_PRIMARY)
            c.roundRect(0, 0, self.width, self.height, 12, fill=1, stroke=0)
            c.setFillColor(white)
            c.setFont('Helvetica-Bold', 22)
            c.drawString(1*cm, self.height - 1.4*cm, self.t)
            if self.st:
                c.setFont('Helvetica', 11)
                c.setFillColor(C_ACCENT)
                c.drawString(1*cm, self.height - 2.2*cm, self.st)

    items.append(Spacer(1, 3*cm))
    items.append(SectionBanner(title, subtitle))
    items.append(PageBreak())
    return items


def intro_section():
    items = []
    items.append(Paragraph("Introduction", ST_H1))
    items.append(HRFlowable(width='100%', thickness=1.5, color=C_PRIMARY, spaceAfter=10))

    items.append(Paragraph("What is JobLinker?", ST_H2))
    items.append(Paragraph(
        "JobLinker is a full-featured Android application built to bridge the gap between "
        "employers who have job openings and candidates who are actively searching for work. "
        "The platform serves as a unified marketplace where both parties can connect, "
        "communicate, and complete the hiring process within a single mobile application.",
        ST_BODY))
    items.append(Spacer(1, 6))
    items.append(Paragraph(
        "Upon registration, each user selects their account type — <b>Employer</b> or "
        "<b>Job Seeker</b> — and the app customizes its features accordingly. "
        "Employers can post vacancies, manage applications, and conduct interviews. "
        "Job seekers can browse listings, apply with a cover letter, and communicate "
        "directly with hiring managers through real-time chat and voice calls.",
        ST_BODY))

    items.append(Spacer(1, 10))
    items.append(Paragraph("Project Background", ST_H2))
    items.append(Paragraph(
        "This project was developed as part of the Computer Science graduation project "
        "requirement at Al-Bayan Comprehensive School, specializing in Systems Planning. "
        "The idea was motivated by the increasing demand for mobile-first job platforms "
        "and the desire to apply modern Android development skills in a real-world context.",
        ST_BODY))

    items.append(Spacer(1, 10))
    items.append(Paragraph("Target Audience", ST_H2))
    rows = [
        ("Employers",    "Business owners, HR managers, and recruiters who need to fill open positions quickly and communicate with candidates."),
        ("Job Seekers",  "Students, fresh graduates, and experienced professionals looking for full-time, part-time, or remote job opportunities."),
        ("Recruiters",   "Third-party recruitment agents who manage multiple employer accounts and track candidates across different companies."),
    ]
    for title, desc in rows:
        items.append(Paragraph(f"• <b>{title}:</b> {desc}", ST_BULLET))
        items.append(Spacer(1, 3))

    items.append(Spacer(1, 10))
    items.append(Paragraph("Key Problems Solved", ST_H2))
    problems = [
        "Fragmented communication — JobLinker consolidates job search, application submission, and employer-candidate messaging in one app.",
        "Slow hiring — Real-time notifications and instant messaging accelerate the hiring timeline.",
        "Limited reach — Advanced search filters and category-based browsing help both parties find the right match faster.",
        "Lack of verification — Email and phone OTP verification reduce fake accounts.",
    ]
    for p in problems:
        items.append(Paragraph(f"• {p}", ST_BULLET))
        items.append(Spacer(1, 3))

    return items


def quick_info_section():
    items = []
    items.append(Paragraph("Quick Info", ST_H1))
    items.append(HRFlowable(width='100%', thickness=1.5, color=C_PRIMARY, spaceAfter=10))

    # Info table
    info_data = [
        [Paragraph("<b>Field</b>", ST_TABLE_HDR), Paragraph("<b>Details</b>", ST_TABLE_HDR)],
        [Paragraph("Application Name",  ST_TABLE_CELL), Paragraph("JobLinker",                             ST_TABLE_CELL)],
        [Paragraph("Platform",          ST_TABLE_CELL), Paragraph("Android (API 24 – Android 7.0+)",       ST_TABLE_CELL)],
        [Paragraph("Language",          ST_TABLE_CELL), Paragraph("Java",                                   ST_TABLE_CELL)],
        [Paragraph("Backend",           ST_TABLE_CELL), Paragraph("Firebase (Auth, Firestore, Storage, FCM)", ST_TABLE_CELL)],
        [Paragraph("Real-time Calls",   ST_TABLE_CELL), Paragraph("Agora RTC SDK",                          ST_TABLE_CELL)],
        [Paragraph("Architecture",      ST_TABLE_CELL), Paragraph("Activity / Fragment (MVVM-inspired)",    ST_TABLE_CELL)],
        [Paragraph("Screens",           ST_TABLE_CELL), Paragraph("20+ Activities and Fragments",           ST_TABLE_CELL)],
        [Paragraph("User Roles",        ST_TABLE_CELL), Paragraph("Employer / Job Seeker",                  ST_TABLE_CELL)],
        [Paragraph("Auth Methods",      ST_TABLE_CELL), Paragraph("Email/Password, Google Sign-In, Phone OTP", ST_TABLE_CELL)],
        [Paragraph("Message Types",     ST_TABLE_CELL), Paragraph("Text, Image, Voice, Document, Location, Contact", ST_TABLE_CELL)],
        [Paragraph("Localization",      ST_TABLE_CELL), Paragraph("English, Arabic, Hebrew",                ST_TABLE_CELL)],
        [Paragraph("GitHub",            ST_TABLE_CELL), Paragraph("github.com/Majd868/JobLinker",           ST_TABLE_CELL)],
        [Paragraph("Developer",         ST_TABLE_CELL), Paragraph("Majd Ahmed Majdoub  •  ID: 217229301",   ST_TABLE_CELL)],
        [Paragraph("Supervisor",        ST_TABLE_CELL), Paragraph("Mr. Iyad Marieh",                        ST_TABLE_CELL)],
        [Paragraph("Submission Date",   ST_TABLE_CELL), Paragraph("April 16, 2026",                         ST_TABLE_CELL)],
    ]
    av = PAGE_W - 3.6*cm
    t = Table(info_data, colWidths=[av*0.28, av*0.72])
    t.setStyle(TableStyle([
        ('BACKGROUND',    (0,0), (-1,0),  C_PRIMARY),
        ('TEXTCOLOR',     (0,0), (-1,0),  white),
        ('ROWBACKGROUNDS',(0,1),(-1,-1), [C_SURFACE, white]),
        ('GRID',          (0,0), (-1,-1), 0.4, C_DIVIDER),
        ('LEFTPADDING',   (0,0), (-1,-1), 8),
        ('RIGHTPADDING',  (0,0), (-1,-1), 8),
        ('TOPPADDING',    (0,0), (-1,-1), 6),
        ('BOTTOMPADDING', (0,0), (-1,-1), 6),
        ('VALIGN',        (0,0), (-1,-1), 'TOP'),
    ]))
    items.append(t)
    items.append(Spacer(1, 16))

    # Statistics
    items.append(Paragraph("Project Statistics", ST_H2))
    stats = [
        ("47",  "Java Source Files"),
        ("30",  "XML Layout Files"),
        ("20+", "Screens / Activities / Fragments"),
        ("7",   "Firestore Collections"),
        ("6",   "Message Types in Chat"),
        ("3",   "Registration Steps"),
    ]
    sw = (PAGE_W - 3.6*cm) / 3
    stat_rows = [stats[i:i+3] for i in range(0, len(stats), 3)]
    for row in stat_rows:
        row_data = [[
            Table([
                [Paragraph(f'<font size="24" color="{C_PRIMARY.hexval()}"><b>{n}</b></font>', ST_BODY)],
                [Paragraph(label, ST_CAPTION)],
            ], colWidths=[sw-12])
            for n, label in row
        ]]
        stat_tbl = Table(row_data, colWidths=[sw]*3)
        stat_tbl.setStyle(TableStyle([
            ('ALIGN',   (0,0),(-1,-1),'CENTER'),
            ('VALIGN',  (0,0),(-1,-1),'MIDDLE'),
            ('BOX',     (0,0),(-1,-1), 0.4, C_DIVIDER),
            ('INNERGRID',(0,0),(-1,-1), 0.4, C_DIVIDER),
            ('BACKGROUND',(0,0),(-1,-1), C_SURFACE),
            ('TOPPADDING',(0,0),(-1,-1), 10),
            ('BOTTOMPADDING',(0,0),(-1,-1), 10),
        ]))
        items.append(stat_tbl)
        items.append(Spacer(1, 4))

    return items


def tech_stack_section():
    items = []
    items.append(Paragraph("Technology Stack", ST_H1))
    items.append(HRFlowable(width='100%', thickness=1.5, color=C_PRIMARY, spaceAfter=10))

    technologies = [
        ("Android SDK (Java)",
         "The primary development platform. Java was chosen for its maturity, broad "
         "documentation, and excellent support in Android Studio. The app targets "
         "API 24 (Android 7.0) and above, covering over 95% of active Android devices.",
         "#3DDC84"),
        ("Firebase Authentication",
         "Handles user sign-in and registration via Email/Password, Google Sign-In, "
         "and Phone Number (OTP via SMS). Manages session tokens and supports email "
         "verification and password reset out of the box.",
         "#FFCA28"),
        ("Firebase Cloud Firestore",
         "NoSQL document database for real-time data sync. Stores all app data: "
         "users, jobs, messages, conversations, applications, saved jobs, calls, and reports. "
         "Real-time listeners power the live chat and job update features.",
         "#FFCA28"),
        ("Firebase Cloud Storage",
         "Object storage for user-uploaded files including profile avatars, "
         "chat images, voice recordings, and document attachments.",
         "#FFCA28"),
        ("Firebase Cloud Messaging (FCM)",
         "Push notification service. Sends notifications for new messages, job applications, "
         "and job alerts. Handled by JobLinkerFireBaseMessagingServices.",
         "#FFCA28"),
        ("Agora RTC SDK",
         "Real-time communication SDK providing voice and video calling capabilities. "
         "Integrated in CallActivity with mute, speaker, and camera toggle controls.",
         "#099DFD"),
        ("Glide",
         "Image loading and caching library by BumpTech. Used throughout the app "
         "for loading profile photos and chat images from URLs with placeholder support.",
         "#4CAF50"),
        ("Material Design Components",
         "Google's design system providing pre-built UI components: BottomNavigationView, "
         "TextInputLayout, ChipGroup, FAB, CardView, TabLayout, ViewPager2, and more.",
         "#6200EE"),
        ("RecyclerView + DiffUtil",
         "Efficient list rendering for jobs, chats, and messages. DiffUtil calculates "
         "the minimum changes needed to update the list, avoiding full redraws.",
         "#3DDC84"),
        ("ViewPager2 + Fragment",
         "Used in LoginActivity (Email/Phone tabs) and RegisterActivity (3-step form). "
         "Provides smooth swipe navigation between fragments.",
         "#3DDC84"),
    ]

    for tech, desc, color in technologies:
        row = Table([
            [
                Paragraph(f'<font color="{color}"><b>⬛</b></font> <b>{tech}</b>', ST_H3),
                Paragraph(desc, ST_BODY_SMALL),
            ]
        ], colWidths=[(PAGE_W-3.6*cm)*0.28, (PAGE_W-3.6*cm)*0.72])
        row.setStyle(TableStyle([
            ('VALIGN',  (0,0),(-1,-1),'TOP'),
            ('BOX',     (0,0),(-1,-1), 0.4, C_DIVIDER),
            ('BACKGROUND',(0,0),(0,0), C_SURFACE),
            ('LEFTPADDING',(0,0),(-1,-1), 8),
            ('RIGHTPADDING',(0,0),(-1,-1), 8),
            ('TOPPADDING',(0,0),(-1,-1), 6),
            ('BOTTOMPADDING',(0,0),(-1,-1), 6),
        ]))
        items.append(row)
        items.append(Spacer(1, 4))

    return items


def project_structure_section():
    items = []
    items.append(Paragraph("Project Structure", ST_H1))
    items.append(HRFlowable(width='100%', thickness=1.5, color=C_PRIMARY, spaceAfter=10))

    items.append(Paragraph(
        "The JobLinker project follows standard Android Studio project conventions "
        "with a clear separation of concerns. The main source code is organized under "
        "<b>app/src/main/java/com/example/joblinker/</b> in the following packages:",
        ST_BODY))
    items.append(Spacer(1, 10))

    packages = [
        ("activities/",   "All Activity classes (screens that occupy the full window). "
                          "Includes SplashActivity, LoginActivity, RegisterActivity, MainActivity, "
                          "ChatActivity, CallActivity, PostJobActivity, JobDetailsActivity, "
                          "EditProfileActivity, FilterActivity, SettingActivity, SavedJobsActivity."),
        ("fragments/",    "Fragment classes hosted inside activities. "
                          "JobsFragment, ChatsFragment, ProfileFragment, "
                          "LoginEmailFragment, LoginPhoneFragment, and the 3 RegisterStep fragments."),
        ("adapters/",     "RecyclerView and ViewPager adapters. "
                          "JobAdapter, ConversationAdapter, MessageAdapter, "
                          "LoginPagerAdapter, RegisterPagerAdapter."),
        ("models/",       "Plain Java data model classes (POJOs) mapped to Firestore documents. "
                          "User, Job, Message, Conversation, Application, Call."),
        ("firebase/",     "Firebase abstraction layer. "
                          "AuthManager (authentication), DatabaseManager (Firestore helpers), "
                          "JobLinkerFirebaseManager (main singleton manager)."),
        ("services/",     "Android services. CallService (foreground service for calls), "
                          "JobLinkerFireBaseMessagingServices (FCM handler)."),
        ("receivers/",    "Broadcast receivers. NetworkChangeReceiver, BootReceiver."),
        ("utils/",        "Utility/helper classes. ValidationHelper, ImageUtils, DateTimeHelper, "
                          "LocaleHelper, LogoutHelper, SharedPreferencesManager, ProgressDialogHelper."),
    ]

    for pkg, desc in packages:
        row = Table([[
            Paragraph(f'<font color="#42A5F5"><b>{pkg}</b></font>', ST_H3),
            Paragraph(desc, ST_BODY_SMALL),
        ]], colWidths=[(PAGE_W-3.6*cm)*0.22, (PAGE_W-3.6*cm)*0.78])
        row.setStyle(TableStyle([
            ('VALIGN',        (0,0),(-1,-1),'TOP'),
            ('BACKGROUND',    (0,0),(0,0),   HexColor('#E8F4FD')),
            ('BOX',           (0,0),(-1,-1), 0.4, C_DIVIDER),
            ('LEFTPADDING',   (0,0),(-1,-1), 8),
            ('RIGHTPADDING',  (0,0),(-1,-1), 8),
            ('TOPPADDING',    (0,0),(-1,-1), 6),
            ('BOTTOMPADDING', (0,0),(-1,-1), 6),
        ]))
        items.append(row)
        items.append(Spacer(1, 3))

    items.append(Spacer(1, 12))
    items.append(Paragraph("Navigation Flow", ST_H2))
    items.append(Paragraph(
        "The diagram below shows how users navigate between screens in the app.",
        ST_BODY))
    items.append(Spacer(1, 8))

    # Navigation flow table
    nav_data = [
        [Paragraph("<b>From</b>", ST_TABLE_HDR),
         Paragraph("<b>To</b>", ST_TABLE_HDR),
         Paragraph("<b>Trigger</b>", ST_TABLE_HDR)],
        [Paragraph("SplashActivity", ST_TABLE_CELL),   Paragraph("MainActivity", ST_TABLE_CELL),      Paragraph("User is logged in (SharedPrefs)", ST_TABLE_CELL)],
        [Paragraph("SplashActivity", ST_TABLE_CELL),   Paragraph("LoginActivity", ST_TABLE_CELL),     Paragraph("User not logged in", ST_TABLE_CELL)],
        [Paragraph("LoginActivity",  ST_TABLE_CELL),   Paragraph("RegisterActivity", ST_TABLE_CELL),  Paragraph("'Sign Up' link tapped", ST_TABLE_CELL)],
        [Paragraph("LoginActivity",  ST_TABLE_CELL),   Paragraph("MainActivity", ST_TABLE_CELL),      Paragraph("Login success", ST_TABLE_CELL)],
        [Paragraph("MainActivity – Jobs",  ST_TABLE_CELL), Paragraph("JobDetailsActivity", ST_TABLE_CELL), Paragraph("Job card tapped", ST_TABLE_CELL)],
        [Paragraph("MainActivity – Jobs",  ST_TABLE_CELL), Paragraph("PostJobActivity", ST_TABLE_CELL),    Paragraph("FAB tapped (Employer only)", ST_TABLE_CELL)],
        [Paragraph("MainActivity – Jobs",  ST_TABLE_CELL), Paragraph("FilterActivity", ST_TABLE_CELL),     Paragraph("Filter icon tapped", ST_TABLE_CELL)],
        [Paragraph("JobDetailsActivity",   ST_TABLE_CELL), Paragraph("ChatActivity", ST_TABLE_CELL),       Paragraph("'Contact Employer' button", ST_TABLE_CELL)],
        [Paragraph("ChatActivity",         ST_TABLE_CELL), Paragraph("CallActivity", ST_TABLE_CELL),       Paragraph("Call button in toolbar", ST_TABLE_CELL)],
        [Paragraph("MainActivity – Profile",ST_TABLE_CELL),Paragraph("EditProfileActivity", ST_TABLE_CELL),Paragraph("Edit Profile button", ST_TABLE_CELL)],
        [Paragraph("MainActivity – Profile",ST_TABLE_CELL),Paragraph("SettingActivity", ST_TABLE_CELL),   Paragraph("Settings button", ST_TABLE_CELL)],
        [Paragraph("SettingActivity",       ST_TABLE_CELL),Paragraph("SavedJobsActivity", ST_TABLE_CELL), Paragraph("'Saved Jobs' row tapped", ST_TABLE_CELL)],
    ]
    av = PAGE_W - 3.6*cm
    nav_t = Table(nav_data, colWidths=[av*0.28, av*0.28, av*0.44])
    nav_t.setStyle(TableStyle([
        ('BACKGROUND',    (0,0), (-1,0),  C_PRIMARY),
        ('TEXTCOLOR',     (0,0), (-1,0),  white),
        ('ROWBACKGROUNDS',(0,1),(-1,-1), [C_SURFACE, white]),
        ('GRID',          (0,0), (-1,-1), 0.4, C_DIVIDER),
        ('LEFTPADDING',   (0,0), (-1,-1), 7),
        ('TOPPADDING',    (0,0), (-1,-1), 5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 5),
        ('VALIGN',        (0,0), (-1,-1), 'TOP'),
    ]))
    items.append(nav_t)

    # ── Visual navigation flow diagram ──
    items.append(PageBreak())
    items.append(Paragraph("Screen Navigation Flow Diagram", ST_H2))
    items.append(Paragraph(
        "The diagram below shows how users navigate between all screens in the JobLinker app. "
        "Blue arrows represent tab/button navigation, green arrows represent success flows, "
        "and orange arrows represent employer-only actions.",
        ST_BODY))
    items.append(Spacer(1, 8))
    av = PAGE_W - 3.6*cm
    items.append(build_nav_flow_diagram(av))

    return items


def build_nav_flow_diagram(avail_w):
    """
    Draw a visual screen-navigation flow diagram using Pillow.
    Returns a ReportLab Image flowable.
    """
    # ── Canvas dimensions ──
    CW, CH = 1700, 2000
    img = PILImage.new('RGB', (CW, CH), (248, 250, 252))
    d   = ImageDraw.Draw(img)

    # ── Mini phone mockup helper ──
    PW, PH = 110, 200   # phone size in canvas pixels

    def phone(cx, cy, title, color=(21, 101, 192)):
        """Draw a mini phone mockup centred at (cx, cy)."""
        x0, y0 = cx - PW//2, cy - PH//2
        x1, y1 = cx + PW//2, cy + PH//2
        # body
        d.rounded_rectangle([x0, y0, x1, y1], radius=14,
                             fill=(26, 26, 46), outline=(80, 80, 110), width=2)
        # notch
        d.ellipse([cx-10, y0+6, cx+10, y0+20], fill=(10, 10, 30))
        # screen area
        sx0, sy0, sx1, sy1 = x0+5, y0+22, x1-5, y1-10
        d.rectangle([sx0, sy0, sx1, sy1], fill=(250, 252, 255))
        # toolbar stripe
        d.rectangle([sx0, sy0, sx1, sy0+28], fill=color)
        # label on toolbar
        try:
            fnt = ImageFont.truetype("arial.ttf", 8)
        except Exception:
            fnt = ImageFont.load_default()
        words = title.split()
        line1 = words[0] if words else title
        line2 = ' '.join(words[1:]) if len(words) > 1 else ''
        tx = sx0 + (sx1-sx0)//2
        ty = sy0 + 8
        d.text((tx, ty), line1, fill=(255,255,255), font=fnt, anchor='mm')
        if line2:
            d.text((tx, ty+10), line2, fill=(255,255,255), font=fnt, anchor='mm')
        # filler content lines
        for i in range(3):
            lx0 = sx0+6
            lx1 = sx1-6 if i < 2 else sx0+(sx1-sx0)*2//3
            d.rounded_rectangle([lx0, sy0+36+i*18, lx1, sy0+46+i*18],
                                 radius=3, fill=(220, 228, 240))
        # home bar
        d.rounded_rectangle([cx-15, y1-16, cx+15, y1-12], radius=2, fill=(80,80,110))

    # ── Arrow helper ──
    def arrow(x1, y1, x2, y2, col=(33, 150, 243), label='', dashed=False):
        """Draw an arrow from (x1,y1) to (x2,y2) with optional label."""
        import math
        steps = 20 if dashed else 1
        dx, dy = x2-x1, y2-y1
        if dashed:
            segs = 12
            for s in range(segs):
                if s % 2 == 0:
                    fx = x1 + dx*s/segs;     fy = y1 + dy*s/segs
                    tx = x1 + dx*(s+0.8)/segs; ty = y1 + dy*(s+0.8)/segs
                    d.line([(fx,fy),(tx,ty)], fill=col, width=2)
        else:
            d.line([(x1,y1),(x2,y2)], fill=col, width=2)
        # arrowhead
        angle = math.atan2(y2-y1, x2-x1)
        ah = 10
        for sign in (1, -1):
            ex = x2 - ah*math.cos(angle - sign*0.4)
            ey = y2 - ah*math.sin(angle - sign*0.4)
            d.line([(x2,y2),(int(ex),int(ey))], fill=col, width=2)
        # label
        if label:
            mx, my = (x1+x2)//2, (y1+y2)//2
            try:
                fnt_lbl = ImageFont.truetype("arial.ttf", 10)
            except Exception:
                fnt_lbl = ImageFont.load_default()
            bbox = d.textbbox((0,0), label, font=fnt_lbl)
            bw, bh = bbox[2]-bbox[0]+8, bbox[3]-bbox[1]+4
            d.rounded_rectangle([mx-bw//2-2, my-bh//2-2, mx+bw//2+2, my+bh//2+2],
                                 radius=4, fill=(255,255,255), outline=col, width=1)
            d.text((mx, my), label, fill=col, font=fnt_lbl, anchor='mm')

    # ── Screen positions (cx, cy) ──
    BLUE   = (21,  101, 192)   # navigation
    GREEN  = (27,  124,  62)   # success / call
    ORANGE = (230,  81,   0)   # employer only
    GRAY   = (100, 100, 120)   # settings / back

    ROW = [260, 520, 780, 1040, 1300, 1560]   # y centres for each row
    COL = [200, 490, 850, 1210, 1500]         # x centres

    # Positions: (name, cx, cy, toolbar_color)
    screens = {
        'Splash':       (CW//2,      ROW[0], (21,101,192)),
        'Login':        (COL[1],     ROW[1], (21,101,192)),
        'Register':     (COL[3],     ROW[1], (21,101,192)),
        'Jobs/Main':    (CW//2,      ROW[2], (21,101,192)),
        'Chats':        (COL[0],     ROW[3], (21,101,192)),
        'Filter':       (COL[1],     ROW[3], (41,182,246)),
        'Job Details':  (COL[2],     ROW[3], (21,101,192)),
        'Profile':      (COL[3],     ROW[3], (21,101,192)),
        'Post Job':     (COL[4],     ROW[3], (230,81,0)),
        'Chat':         (COL[0],     ROW[4], (21,101,192)),
        'Call':         (COL[1],     ROW[4], (27,124,62)),
        'Saved Jobs':   (COL[2],     ROW[4], (21,101,192)),
        'Edit Profile': (COL[3],     ROW[4], (21,101,192)),
        'Settings':     (COL[4],     ROW[4], (100,100,120)),
    }

    # Draw all phone mockups
    for name, (cx, cy, col) in screens.items():
        phone(cx, cy, name, color=col)

    # ── Draw arrows ──
    def edge(frm, to, col=BLUE, label='', dashed=False):
        fx, fy, _ = screens[frm]
        tx, ty, _ = screens[to]
        # adjust endpoints to phone edges
        dx, dy = tx-fx, ty-fy
        import math
        dist = math.hypot(dx, dy) or 1
        sx = fx + dx/dist * (PW//2 + 4)
        sy = fy + dy/dist * (PH//2 + 4)
        ex = tx - dx/dist * (PW//2 + 4)
        ey = ty - dy/dist * (PH//2 + 4)
        arrow(int(sx), int(sy), int(ex), int(ey), col=col, label=label, dashed=dashed)

    # Splash → Login / Register
    edge('Splash', 'Login',    col=BLUE,  label='Not logged in')
    edge('Splash', 'Register', col=BLUE,  label='Not logged in')

    # Login ↔ Register
    edge('Login', 'Register',  col=GRAY,  label='Sign Up', dashed=True)

    # Login / Register → Jobs/Main
    edge('Login',    'Jobs/Main', col=GREEN, label='Success')
    edge('Register', 'Jobs/Main', col=GREEN, label='Complete')

    # Jobs/Main → 4 children
    edge('Jobs/Main', 'Chats',      col=BLUE,   label='Tab')
    edge('Jobs/Main', 'Filter',     col=BLUE,   label='Filter icon')
    edge('Jobs/Main', 'Job Details',col=BLUE,   label='Card tap')
    edge('Jobs/Main', 'Profile',    col=BLUE,   label='Tab')
    edge('Jobs/Main', 'Post Job',   col=ORANGE, label='FAB +')

    # Row-4 children
    edge('Chats',      'Chat',         col=BLUE,  label='Open chat')
    edge('Chat',       'Call',         col=GREEN, label='Call btn')
    edge('Job Details','Saved Jobs',   col=BLUE,  label='Bookmark')
    edge('Profile',    'Edit Profile', col=BLUE,  label='Edit')
    edge('Profile',    'Settings',     col=GRAY,  label='Settings')

    # ── Legend ──
    lx, ly = 60, CH - 130
    try:
        fnt_leg = ImageFont.truetype("arial.ttf", 13)
    except Exception:
        fnt_leg = ImageFont.load_default()
    legend = [
        (BLUE,   'Tab / button navigation'),
        (GREEN,  'Success / call flow'),
        (ORANGE, 'Employer only'),
        (GRAY,   'Settings / secondary'),
    ]
    d.text((lx, ly-20), 'Legend:', fill=(30,30,50), font=fnt_leg)
    for i, (col, txt) in enumerate(legend):
        bx = lx + i*380
        d.line([(bx, ly+10), (bx+40, ly+10)], fill=col, width=3)
        d.polygon([(bx+40, ly+5), (bx+50, ly+10), (bx+40, ly+15)], fill=col)
        d.text((bx+58, ly+10), txt, fill=(30,30,50), font=fnt_leg, anchor='lm')

    # ── Convert to ReportLab Image ──
    buf = io.BytesIO()
    img.save(buf, format='PNG')
    buf.seek(0)
    scale = avail_w / CW
    return RLImage(buf, width=avail_w, height=CH*scale)


def build_architecture_diagram(avail_w):
    """
    Returns a Flowable that draws the full system architecture UML
    showing ALL 48 classes organized by package, with dependency arrows.
    """
    from reportlab.platypus import Flowable

    class ArchDiagram(Flowable):

        # All packages + their classes
        PACKAGES = [
            # (pkg_name, stereotype, classes_list, col, row, color_rgb)
            ("activities",
             "<<Controller>>",
             ["SplashActivity","LoginActivity","RegisterActivity","MainActivity",
              "PostJobActivity","JobDetailsActivity","ChatActivity","CallActivity",
              "EditProfileActivity","FilterActivity","SettingActivity",
              "SavedJobsActivity","EmailVerificationActivity","BaseActivity"],
             0, 0, (0.082, 0.396, 0.753)),   # blue

            ("fragments",
             "<<View>>",
             ["JobsFragment","ChatsFragment","ProfileFragment",
              "LoginEmailFragment","LoginPhoneFragment",
              "RegisterStep1Fragment","RegisterStep2Fragment","RegisterStep3Fragment"],
             1, 0, (0.145, 0.600, 0.400)),   # green

            ("models",
             "<<Entity>>",
             ["User","Job","Application","Conversation","Message","Call"],
             2, 0, (0.557, 0.267, 0.678)),   # purple

            ("firebase",
             "<<Service>>",
             ["AuthManager","DatabaseManager","JobLinkerFirebaseManager"],
             0, 1, (1.000, 0.600, 0.000)),   # orange

            ("adapters",
             "<<Adapter>>",
             ["JobAdapter","ConversationAdapter","MessageAdapter",
              "LoginPagerAdapter","RegisterPagerAdapter"],
             1, 1, (0.000, 0.588, 0.533)),   # teal

            ("services",
             "<<Service>>",
             ["CallService","JobLinkerFireBaseMessagingServices"],
             2, 1, (0.776, 0.235, 0.200)),   # red

            ("receivers",
             "<<Receiver>>",
             ["NetworkChangeReceiver","BootReceiver"],
             0, 2, (0.404, 0.227, 0.718)),   # violet

            ("utils",
             "<<Utility>>",
             ["ValidationHelper","ImageUtils","DateTimeHelper","LocaleHelper",
              "LogoutHelper","SharedPreferencesManager","ProgressDialogHelper"],
             1, 2, (0.231, 0.510, 0.690)),   # steel blue

            ("application",
             "<<Application>>",
             ["JobLinkerApplication"],
             2, 2, (0.400, 0.400, 0.420)),   # gray
        ]

        # Inter-package dependency arrows (from_pkg, to_pkg, label)
        DEPS = [
            ("activities", "fragments",  "hosts"),
            ("activities", "models",     "uses"),
            ("activities", "firebase",   "calls"),
            ("fragments",  "models",     "binds"),
            ("fragments",  "firebase",   "queries"),
            ("fragments",  "adapters",   "drives"),
            ("adapters",   "models",     "renders"),
            ("firebase",   "models",     "maps"),
            ("services",   "firebase",   "writes"),
            ("receivers",  "activities", "starts"),
            ("activities", "utils",      "uses"),
            ("fragments",  "utils",      "uses"),
        ]

        HDR_H   = 22    # package header height
        ROW_H   = 13    # class-name row height
        FPAD    = 4     # top/bottom padding inside box
        COL_GAP = 14    # gap between columns
        ROW_GAP = 14    # gap between rows
        NUM_COLS = 3

        CLR_WHT = (1, 1, 1)
        CLR_BG  = (0.976, 0.978, 0.992)
        CLR_BRD = (0.700, 0.720, 0.780)
        CLR_TXT = (0.118, 0.137, 0.196)
        CLR_ARR = (0.360, 0.380, 0.440)

        def pkg_h(self, n_classes):
            return self.HDR_H + self.FPAD + n_classes * self.ROW_H + self.FPAD

        def __init__(self, avail_w):
            Flowable.__init__(self)
            self.avail_w = avail_w

            # compute column widths (equal thirds)
            gap_total = self.COL_GAP * (self.NUM_COLS - 1)
            self.col_w = (avail_w - gap_total) / self.NUM_COLS

            # compute row heights per grid row
            row_max_h = {}
            for _, _, classes, col, row, _ in self.PACKAGES:
                h = self.pkg_h(len(classes))
                row_max_h[row] = max(row_max_h.get(row, 0), h)
            self._row_max_h = row_max_h

            num_rows = max(r for _, _, _, _, r, _ in self.PACKAGES) + 1
            total_h = sum(row_max_h.values()) + self.ROW_GAP * (num_rows - 1) + 40
            self.width  = avail_w
            self.height = total_h

        def _pkg_pos(self):
            """Return {pkg_name: (px, py_bottom, pw, ph)}."""
            num_rows = max(r for _, _, _, _, r, _ in self.PACKAGES) + 1
            # row y-bottoms (from bottom of drawing)
            row_bot = {}
            y = 20
            for row_idx in range(num_rows - 1, -1, -1):
                row_bot[row_idx] = y
                y += self._row_max_h[row_idx] + self.ROW_GAP

            pos = {}
            for name, _, classes, col, row, _ in self.PACKAGES:
                px = col * (self.col_w + self.COL_GAP)
                py = row_bot[row]
                ph = self.pkg_h(len(classes))
                pos[name] = (px, py, self.col_w, ph)
            return pos

        def draw(self):
            c = self.canv
            pos = self._pkg_pos()

            # ── Draw package boxes ──────────────────────────
            for name, stereo, classes, col, row, color in self.PACKAGES:
                px, py, pw, ph = pos[name]

                # Shadow
                c.setFillColorRGB(0.72, 0.74, 0.78)
                c.roundRect(px+3, py-3, pw, ph, 5, fill=1, stroke=0)

                # Box body
                c.setFillColorRGB(*self.CLR_BG)
                c.setStrokeColorRGB(*self.CLR_BRD)
                c.setLineWidth(0.8)
                c.roundRect(px, py, pw, ph, 5, fill=1, stroke=1)

                # Header bar
                c.setFillColorRGB(*color)
                c.roundRect(px, py + ph - self.HDR_H, pw, self.HDR_H, 5, fill=1, stroke=0)
                # Cover bottom-rounded of header bar
                c.rect(px+1, py + ph - self.HDR_H, pw-2, self.HDR_H//2, fill=1, stroke=0)

                # Stereotype in header
                c.setFont('Helvetica', 6)
                c.setFillColorRGB(0.85, 0.92, 1.0)
                c.drawCentredString(px + pw/2, py + ph - 9, stereo)

                # Package name
                c.setFont('Helvetica-Bold', 9)
                c.setFillColorRGB(*self.CLR_WHT)
                c.drawCentredString(px + pw/2, py + ph - self.HDR_H + 5.5,
                                    f'{name}/')

                # Separator
                c.setStrokeColorRGB(*self.CLR_BRD)
                c.setLineWidth(0.5)
                sep_y = py + ph - self.HDR_H - 0.5
                c.line(px+2, sep_y, px+pw-2, sep_y)

                # Class name rows
                fy = sep_y - self.FPAD
                for i, cls_name in enumerate(classes):
                    fy -= self.ROW_H
                    # Alternating row bg
                    if i % 2 == 0:
                        c.setFillColorRGB(0.962, 0.966, 0.982)
                        c.rect(px+1, fy, pw-2, self.ROW_H, fill=1, stroke=0)
                    # Class name
                    c.setFont('Courier', 7.5)
                    c.setFillColorRGB(*self.CLR_TXT)
                    c.drawString(px + 7, fy + 3, cls_name)
                    # Small dot indicator
                    dot_col = color
                    c.setFillColorRGB(*dot_col)
                    c.circle(px + pw - 9, fy + 6, 2.5, fill=1, stroke=0)

                # Re-draw border on top
                c.setStrokeColorRGB(*self.CLR_BRD)
                c.setLineWidth(0.8)
                c.roundRect(px, py, pw, ph, 5, fill=0, stroke=1)

            # ── Draw dependency arrows ──────────────────────
            import math
            c.setLineWidth(1.0)

            drawn_pairs = set()
            for from_pkg, to_pkg, dep_label in self.DEPS:
                if from_pkg not in pos or to_pkg not in pos:
                    continue
                pair = tuple(sorted([from_pkg, to_pkg]))
                if pair in drawn_pairs:
                    continue
                drawn_pairs.add(pair)

                px1, py1, pw1, ph1 = pos[from_pkg]
                px2, py2, pw2, ph2 = pos[to_pkg]

                cx1 = px1 + pw1/2
                cy1 = py1 + ph1/2
                cx2 = px2 + pw2/2
                cy2 = py2 + ph2/2

                # Find edge intersection points
                dx, dy = cx2 - cx1, cy2 - cy1
                dist = math.sqrt(dx*dx + dy*dy)
                if dist < 1:
                    continue
                ux, uy = dx/dist, dy/dist

                # Start from border of box 1
                # Approximate: use center + half-width in direction
                half_w1 = pw1/2
                half_h1 = ph1/2
                if abs(ux) > 0:
                    t_w = half_w1 / abs(ux)
                else:
                    t_w = 1e9
                if abs(uy) > 0:
                    t_h = half_h1 / abs(uy)
                else:
                    t_h = 1e9
                t1 = min(t_w, t_h)
                x1s = cx1 + ux * t1
                y1s = cy1 + uy * t1

                # End at border of box 2
                half_w2 = pw2/2
                half_h2 = ph2/2
                if abs(ux) > 0:
                    t_w2 = half_w2 / abs(ux)
                else:
                    t_w2 = 1e9
                if abs(uy) > 0:
                    t_h2 = half_h2 / abs(uy)
                else:
                    t_h2 = 1e9
                t2 = min(t_w2, t_h2)
                x2e = cx2 - ux * t2
                y2e = cy2 - uy * t2

                # Draw dashed dependency arrow
                c.setDash([4, 3], 0)
                c.setStrokeColorRGB(*self.CLR_ARR)
                c.setFillColorRGB(*self.CLR_ARR)
                c.line(x1s, y1s, x2e + ux*6, y2e + uy*6)
                c.setDash([], 0)

                # Open arrowhead (dependency)
                ah = 7
                px_a, py_a = -uy, ux
                c.setLineWidth(1.0)
                c.line(x2e, y2e,
                       x2e - ah*ux + ah*0.38*px_a,
                       y2e - ah*uy + ah*0.38*py_a)
                c.line(x2e, y2e,
                       x2e - ah*ux - ah*0.38*px_a,
                       y2e - ah*uy - ah*0.38*py_a)

                # Label
                mid_x = (x1s + x2e) / 2
                mid_y = (y1s + y2e) / 2
                c.setFont('Helvetica-Oblique', 6.5)
                c.setFillColorRGB(0.35, 0.38, 0.46)
                tw = c.stringWidth(f'<<{dep_label}>>', 'Helvetica-Oblique', 6.5)
                c.setFillColorRGB(1, 1, 1)
                c.rect(mid_x - tw/2 - 2, mid_y - 4, tw + 4, 9, fill=1, stroke=0)
                c.setFillColorRGB(0.35, 0.38, 0.46)
                c.drawCentredString(mid_x, mid_y, f'<<{dep_label}>>')

            # ── Class count footer ──────────────────────────
            c.setFont('Helvetica-Bold', 8)
            c.setFillColorRGB(0.35, 0.38, 0.46)
            total_classes = sum(len(cl) for _, _, cl, _, _, _ in self.PACKAGES)
            c.drawCentredString(self.avail_w/2, 6,
                                f'Total: {total_classes} classes across 9 packages')

    return ArchDiagram(avail_w)


def build_full_uml_image(avail_w):
    """
    Comprehensive UML class diagram using Pillow.
    Layers: Firebase/App → Activities → Fragments → Data Models.
    Each class shows stereotype, fields, and methods.
    """
    import math

    CW, CH = 1870, 1100
    img = PILImage.new('RGB', (CW, CH), (245, 247, 250))
    d   = ImageDraw.Draw(img)

    try:
        fn_bd  = ImageFont.truetype("arialbd.ttf", 11)
        fn_rg  = ImageFont.truetype("arial.ttf",    9)
        fn_it  = ImageFont.truetype("ariali.ttf",   9)
        fn_sm  = ImageFont.truetype("arial.ttf",    8)
        fn_sec = ImageFont.truetype("arialbd.ttf", 13)
    except Exception:
        fn_bd = fn_rg = fn_it = fn_sm = fn_sec = ImageFont.load_default()

    BW, HDR, FH, PAD = 250, 38, 14, 5

    def box_h(flds, meths):
        h = HDR
        if flds:  h += 1 + PAD + len(flds)  * FH + PAD
        if meths: h += 1 + PAD + len(meths) * FH + PAD
        return h

    # section backgrounds
    for x0, y0, x1, y1, col, lbl in [
        (0,   0, CW,  268, (255,250,235), 'Firebase & Application Layer'),
        (0, 268, CW,  740, (233,240,255), 'Activities Layer'),
        (0, 740, CW,  900, (228,246,232), 'Fragments Layer'),
        (0, 900, CW,   CH, (247,232,255), 'Data Model Layer'),
    ]:
        d.rectangle([x0, y0, x1, y1], fill=col)
        d.text((x0+14, y0+6), lbl, fill=(90, 90, 120), font=fn_sec)

    # (name, stereotype, [(field,type),...], [method,...], x, y, hdr_col, bg_col)
    CDATA = [
        # ── Firebase / App layer ──
        ('JobLinkerApplication', '<<Application>>',
         [('+INSTANCE','static JLA'),('+context','Application')],
         ['+onCreate()'],
         390, 50, (60,60,90), (228,228,244)),
        ('AuthManager', '<<Service>>',
         [('+firebaseAuth','FirebaseAuth')],
         ['+login(email,pw,cb)','+register(e,pw,cb)','+loginPhone(ph,cb)','+logout()'],
         670, 50, (180,100,0), (255,248,224)),
        ('JobLinkerFirebaseManager', '<<Singleton>>',
         [('-instance','JFM'),('-auth','FirebaseAuth'),('-db','Firestore'),('-storage','Storage')],
         ['+getInstance():JFM','+getUser(id,cb)','+saveJob(job,cb)','+getJobs(f,cb)','+sendMessage(m,cb)'],
         950, 50, (180,100,0), (255,248,224)),
        ('DatabaseManager', '<<Repository>>',
         [('+db','FirebaseFirestore')],
         ['+getDoc(c,id,cb)','+setDoc(c,id,d,cb)','+query(c,conds,cb)'],
         1230, 50, (180,100,0), (255,248,224)),
        # ── BaseActivity ──
        ('BaseActivity', '<<Abstract>>',
         [],
         ['+attachBaseContext(ctx)'],
         810, 278, (21,101,192), (228,238,255)),
        # ── Activities row 1 ──
        ('SplashActivity',    '<<Activity>>', [('+handler','Handler')],
         ['+onCreate(b)','-checkLogin()'],          50, 378, (21,101,192),(228,238,255)),
        ('LoginActivity',     '<<Activity>>', [('+viewPager','ViewPager2'),('+tabs','TabLayout')],
         ['+onCreate(b)'],                         354, 378, (21,101,192),(228,238,255)),
        ('RegisterActivity',  '<<Activity>>', [('+step','int'),('+viewPager','ViewPager2')],
         ['+onCreate(b)','-nextStep()','-validate()'], 658, 378, (21,101,192),(228,238,255)),
        ('MainActivity',      '<<Activity>>', [('+bottomNav','BottomNav'),('+curFrag','Fragment')],
         ['+onCreate(b)','-setupNav()'],            962, 378, (21,101,192),(228,238,255)),
        ('PostJobActivity',   '<<Activity>>', [('+jobTitle','EditText'),('+desc','EditText')],
         ['+onCreate(b)','-postJob()','-validateForm()'],1266, 378,(21,101,192),(228,238,255)),
        ('JobDetailsActivity','<<Activity>>', [('+jobId','String'),('+job','Job')],
         ['+onCreate(b)','-loadJob()','-applyJob()','-saveJob()'],1570,378,(21,101,192),(228,238,255)),
        # ── Activities row 2 ──
        ('ChatActivity',         '<<Activity>>',
         [('+convId','String'),('+msgs','List<Msg>'),('+adapter','MsgAdapter')],
         ['+onCreate(b)','-sendMessage()','-loadMessages()'],
          50, 563, (21,101,192),(228,238,255)),
        ('CallActivity',         '<<Activity>>',
         [('+engine','RtcEngine'),('+callType','String')],
         ['+onCreate(b)','-joinChannel()','-leaveChannel()'],
         354, 563, (21,101,192),(228,238,255)),
        ('EditProfileActivity',  '<<Activity>>',
         [('+user','User'),('+imageUri','Uri')],
         ['+onCreate(b)','-uploadAvatar()','-saveProfile()'],
         658, 563, (21,101,192),(228,238,255)),
        ('FilterActivity',       '<<Activity>>',
         [('+filterState','FilterState')],
         ['+onCreate(b)','-applyFilter()','-reset()'],
         962, 563, (21,101,192),(228,238,255)),
        ('SettingActivity',      '<<Activity>>',
         [('+prefs','SharedPreferences')],
         ['+onCreate(b)','-logout()','-changeLang()'],
         1266, 563, (21,101,192),(228,238,255)),
        ('SavedJobsActivity',    '<<Activity>>',
         [('+jobs','List<Job>'),('+adapter','JobAdapter')],
         ['+onCreate(b)','-loadSavedJobs()'],
         1570, 563, (21,101,192),(228,238,255)),
        # ── Fragments ──
        ('JobsFragment',    '<<Fragment>>',
         [('+jobs','List<Job>'),('+adapter','JobAdapter')],
         ['+onCreateView()','-loadJobs()','-applyFilter()'],
          530, 755, (27,124,62),(224,245,228)),
        ('ChatsFragment',   '<<Fragment>>',
         [('+convs','List<Conv>'),('+adapter','ConvAdapter')],
         ['+onCreateView()','-loadChats()'],
          810, 755, (27,124,62),(224,245,228)),
        ('ProfileFragment', '<<Fragment>>',
         [('+user','User')],
         ['+onCreateView()','-loadProfile()'],
         1090, 755, (27,124,62),(224,245,228)),
        # ── Data models ──
        ('User',         '<<Entity>>',
         [('+userId','String'),('+userName','String'),('+userEmail','String'),('+userRole','String'),('+isOnline','boolean')],
         [],  50, 910, (106,27,154),(243,228,255)),
        ('Job',          '<<Entity>>',
         [('+jobId','String'),('+jobTitle','String'),('+employerId','String'),('+isActive','boolean')],
         [], 354, 910, (106,27,154),(243,228,255)),
        ('Conversation', '<<Entity>>',
         [('+convId','String'),('+participants','List'),('+lastMsg','String'),('+unread','int')],
         [], 658, 910, (106,27,154),(243,228,255)),
        ('Application',  '<<Entity>>',
         [('+appId','String'),('+jobId','String'),('+userId','String'),('+status','String')],
         [], 962, 910, (106,27,154),(243,228,255)),
        ('Message',      '<<Entity>>',
         [('+msgId','String'),('+convId','String'),('+senderId','String'),('+msgType','String')],
         [],1266, 910, (106,27,154),(243,228,255)),
        ('Call',         '<<Entity>>',
         [('+callId','String'),('+callerId','String'),('+receiverId','String'),('+callType','String')],
         [],1570, 910, (106,27,154),(243,228,255)),
    ]

    # compute box dict: name → (x, y, BW, h)
    # CDATA format: (name, stereo, flds, meths, x, y, hc, bc)  → indices 4=x, 5=y
    boxes = {item[0]: (item[4], item[5], BW, box_h(item[2], item[3])) for item in CDATA}

    def draw_box(name, stereo, flds, meths, x, y, hc, bc):
        h = box_h(flds, meths)
        d.rounded_rectangle([x+3,y+3,x+BW+3,y+h+3], radius=6, fill=(195,200,215))
        d.rounded_rectangle([x,y,x+BW,y+h],          radius=6, fill=bc, outline=(175,185,210), width=1)
        d.rounded_rectangle([x,y,x+BW,y+HDR],        radius=6, fill=hc)
        d.rectangle([x,y+HDR-7,x+BW,y+HDR],          fill=hc)
        d.text((x+BW//2, y+10), stereo,  fill=(200,228,255), font=fn_sm, anchor='mm')
        d.text((x+BW//2, y+27), name,    fill=(255,255,255), font=fn_bd, anchor='mm')
        fy = y + HDR
        if flds:
            d.line([(x+1,fy),(x+BW-1,fy)], fill=(175,185,210), width=1)
            fy += PAD
            for i,(fn_n,ft) in enumerate(flds):
                if i%2==0: d.rectangle([x+1,fy,x+BW-1,fy+FH-1], fill=(235,240,252))
                d.text((x+7, fy+2), fn_n, fill=(30,45,80),  font=fn_rg)
                d.text((x+BW-6,fy+2), ft,  fill=(90,100,145),font=fn_it, anchor='ra')
                fy += FH
            fy += PAD
        if meths:
            d.line([(x+1,fy),(x+BW-1,fy)], fill=(175,185,210), width=1)
            fy += PAD
            for i,mt in enumerate(meths):
                if i%2==0: d.rectangle([x+1,fy,x+BW-1,fy+FH-1], fill=(228,238,252))
                d.text((x+7,fy+2), mt, fill=(20,70,165), font=fn_rg)
                fy += FH
            fy += PAD

    for item in CDATA:
        draw_box(*item)

    # ── Arrow helpers ──────────────────────────────────────
    INH = (21,101,192); DEP = (130,80,180); ASS = (180,80,0); HST = (27,124,62)

    def seg(x1,y1,x2,y2,col,w=1,dash=False):
        if dash:
            n = max(1,int(math.hypot(x2-x1,y2-y1)/8))
            for i in range(n):
                if i%2==0:
                    t0,t1 = i/n, min((i+0.6)/n,1)
                    d.line([(x1+(x2-x1)*t0,y1+(y2-y1)*t0),(x1+(x2-x1)*t1,y1+(y2-y1)*t1)],fill=col,width=w)
        else:
            d.line([(x1,y1),(x2,y2)],fill=col,width=w)

    def atip(x2,y2,dx,dy,col,sz=9):
        ln=math.hypot(dx,dy)
        if ln<1: return
        ux,uy=dx/ln,dy/ln; px,py=-uy,ux
        d.polygon([(int(x2),int(y2)),(int(x2-sz*ux+sz*.38*px),int(y2-sz*uy+sz*.38*py)),(int(x2-sz*ux-sz*.38*px),int(y2-sz*uy-sz*.38*py))],fill=col)

    def htip(x2,y2,dx,dy,col,sz=11):
        ln=math.hypot(dx,dy)
        if ln<1: return
        ux,uy=dx/ln,dy/ln; px,py=-uy,ux
        d.polygon([(int(x2),int(y2)),(int(x2-sz*ux+sz*.42*px),int(y2-sz*uy+sz*.42*py)),(int(x2-sz*ux-sz*.42*px),int(y2-sz*uy-sz*.42*py))],outline=col,fill=(240,244,252))

    def cxb(n): return boxes[n][0]+BW//2
    def top(n): return boxes[n][1]
    def bot(n): return boxes[n][1]+boxes[n][3]

    # 1. Inheritance tree (BaseActivity → Activities)
    r1 = ['SplashActivity','LoginActivity','RegisterActivity','MainActivity','PostJobActivity','JobDetailsActivity']
    r2 = ['ChatActivity','CallActivity','EditProfileActivity','FilterActivity','SettingActivity','SavedJobsActivity']
    bus1 = 366; bus2 = 551
    ba_cx = cxb('BaseActivity'); ba_b = bot('BaseActivity')
    r1x = [cxb(a) for a in r1]; r2x = [cxb(a) for a in r2]
    # BaseActivity → bus1
    seg(ba_cx, ba_b, ba_cx, bus1, INH, 2)
    htip(ba_cx, ba_b, 0, 1, INH, 11)
    # bus1 horizontal + drops to each r1 activity
    seg(min(r1x), bus1, max(r1x), bus1, INH, 2)
    for ax, a in zip(r1x, r1):
        seg(ax, bus1, ax, top(a), INH, 1)
    # extend bus1 leftmost down to bus2, then bus2
    seg(min(r1x), bus1, min(r1x), bus2, INH, 2)
    seg(min(r2x), bus2, max(r2x), bus2, INH, 2)
    for ax, a in zip(r2x, r2):
        seg(ax, bus2, ax, top(a), INH, 1)

    # 2. Dependency: activities → FirebaseManager (dashed purple)
    fbm_cx = cxb('JobLinkerFirebaseManager'); fbm_b = bot('JobLinkerFirebaseManager')
    for src in ['MainActivity','ChatActivity']:
        sc = cxb(src); st = top(src)
        seg(sc, st, fbm_cx, fbm_b+1, DEP, 1, dash=True)
        atip(fbm_cx, fbm_b+1, fbm_cx-sc, fbm_b-st, DEP, 8)

    # 3. Hosting: MainActivity → Fragments (dashed green)
    mc = cxb('MainActivity'); mb = bot('MainActivity')
    mid_y = (mb + top('JobsFragment')) // 2
    seg(mc, mb, mc, mid_y, HST, 1, dash=True)
    for fr in ['JobsFragment','ChatsFragment','ProfileFragment']:
        fc = cxb(fr); ft = top(fr)
        seg(mc, mid_y, fc, mid_y, HST, 1, dash=True)
        seg(fc, mid_y, fc, ft, HST, 1, dash=True)
        atip(fc, ft, 0, 1, HST, 7)

    # 4. Association: FirebaseManager → Models (dashed orange)
    for m in ['User','Job','Conversation','Application','Message','Call']:
        mc_m = cxb(m); mt = top(m)
        mid_x = (fbm_cx + mc_m) // 2
        seg(fbm_cx, fbm_b+2, mid_x, mt-4, ASS, 1, dash=True)
        seg(mid_x, mt-4, mc_m, mt, ASS, 1, dash=True)
        atip(mc_m, mt, 0, 1, ASS, 7)

    # legend
    try: fn_lg = ImageFont.truetype("arial.ttf", 11)
    except: fn_lg = ImageFont.load_default()
    d.text((30, CH-58), 'Legend:', fill=(40,40,60), font=fn_sec)
    for i,(col,dsh,txt) in enumerate([
        (INH,False,'Inheritance (extends)'),
        (DEP,True, 'Dependency / Uses'),
        (ASS,True, 'Association / Manages'),
        (HST,True, 'Hosting / Contains'),
    ]):
        lbx = 30 + i*380
        seg(lbx, CH-32, lbx+40, CH-32, col, 2, dash=dsh)
        atip(lbx+40, CH-32, 1, 0, col, 7)
        d.text((lbx+50, CH-32), txt, fill=(40,40,60), font=fn_lg, anchor='lm')

    buf = io.BytesIO(); img.save(buf, format='PNG'); buf.seek(0)
    scale = avail_w / CW
    return RLImage(buf, width=avail_w, height=CH*scale)


def build_user_flow_image(avail_w, title, nodes, edges):
    """
    Draw a user-flow diagram.
    nodes : [(id, label, content_lines, cx, cy), ...]
    edges : [(from_id, to_id, label, color_rgb, dashed=False), ...]
      - solid colored  = forward navigation
      - dashed gray    = back / return navigation
    Bidirectional pairs are automatically offset so arrows don't overlap.
    """
    import math

    SW, SH   = 155, 230    # phone size px
    LEGEND_H = 90          # legend strip height

    max_cx = max(n[3] for n in nodes) if nodes else 600
    max_cy = max(n[4] for n in nodes) if nodes else 600
    CW = int(max_cx + SW // 2 + 70)
    CH = int(max_cy + SH // 2 + LEGEND_H + 30)

    img = PILImage.new('RGB', (CW, CH), (245, 247, 250))
    d   = ImageDraw.Draw(img)

    # subtle grid background
    for gx in range(0, CW, 40):
        d.line([(gx, 0), (gx, CH)], fill=(230, 233, 240), width=1)
    for gy in range(0, CH, 40):
        d.line([(0, gy), (CW, gy)], fill=(230, 233, 240), width=1)

    try:
        fn_bd  = ImageFont.truetype("arialbd.ttf", 12)
        fn_reg = ImageFont.truetype("arial.ttf",   10)
        fn_sm  = ImageFont.truetype("arial.ttf",    9)
        fn_ti  = ImageFont.truetype("arialbd.ttf", 17)
        fn_lbl = ImageFont.truetype("arialbd.ttf", 10)
    except Exception:
        fn_bd = fn_reg = fn_sm = fn_ti = fn_lbl = ImageFont.load_default()

    # title banner
    d.rectangle([0, 0, CW, 52], fill=(21, 101, 192))
    # subtle stripe
    d.rectangle([0, 48, CW, 52], fill=(66, 165, 245))
    d.text((CW // 2, 26), title, fill=(255, 255, 255), font=fn_ti, anchor='mm')

    node_pos  = {n[0]: (n[3], n[4]) for n in nodes}
    edge_set  = {(e[0], e[1]) for e in edges}
    # pairs that have BOTH directions → need perpendicular offset
    bidi_set  = {(a, b) for (a, b) in edge_set if (b, a) in edge_set}
    PERP_OFF  = 9   # pixels of perpendicular separation

    # ── ellipse-border clip ─────────────────────────────────────
    def clip(cx, cy, tx, ty, pad):
        dx, dy = tx - cx, ty - cy
        ln = math.hypot(dx, dy)
        if ln < 1: return cx, cy
        ux, uy = dx / ln, dy / ln
        rx, ry = SW // 2 + pad, SH // 2 + pad
        den = math.sqrt((ux/rx)**2 + (uy/ry)**2)
        t = 1 / den if den > 0 else rx
        return int(cx + ux * t), int(cy + uy * t)

    # ── single-edge draw ───────────────────────────────────────
    def draw_one(from_id, to_id, label, col, dashed):
        cx1, cy1 = node_pos[from_id]
        cx2, cy2 = node_pos[to_id]
        x1, y1 = clip(cx1, cy1, cx2, cy2, pad=6)
        x2, y2 = clip(cx2, cy2, cx1, cy1, pad=14)

        # perpendicular offset for bidirectional pairs
        if (from_id, to_id) in bidi_set:
            dx0, dy0 = x2 - x1, y2 - y1
            ln0 = math.hypot(dx0, dy0)
            if ln0 > 0:
                px, py = -dy0/ln0, dx0/ln0
                x1 = int(x1 + px * PERP_OFF); y1 = int(y1 + py * PERP_OFF)
                x2 = int(x2 + px * PERP_OFF); y2 = int(y2 + py * PERP_OFF)

        dx, dy = x2 - x1, y2 - y1
        ln = math.hypot(dx, dy)
        if ln < 1: return
        ux, uy = dx / ln, dy / ln
        px2, py2 = -uy, ux

        if dashed:
            # dashed thin gray line
            n_seg = max(1, int(ln / 10))
            for i in range(n_seg):
                if i % 2 == 0:
                    t0, t1 = i / n_seg, min((i + 0.65) / n_seg, 1.0)
                    d.line([(int(x1 + dx*t0), int(y1 + dy*t0)),
                             (int(x1 + dx*t1), int(y1 + dy*t1))],
                            fill=col, width=2)
        else:
            # solid with shadow
            d.line([(x1+2, y1+2), (x2+2, y2+2)], fill=(175, 180, 200), width=4)
            d.line([(x1,   y1  ), (x2,   y2  )], fill=col, width=3)

        # arrowhead
        sz = 10 if dashed else 13
        d.polygon([
            (int(x2), int(y2)),
            (int(x2 - sz*ux + sz*.42*px2), int(y2 - sz*uy + sz*.42*py2)),
            (int(x2 - sz*ux - sz*.42*px2), int(y2 - sz*uy - sz*.42*py2)),
        ], fill=col)

        # label bubble
        mx, my = (x1 + x2) // 2, (y1 + y2) // 2
        try:
            bb = d.textbbox((0, 0), label, font=fn_lbl)
            bw, bh = bb[2] - bb[0] + 14, bb[3] - bb[1] + 7
        except Exception:
            bw, bh = 80, 16
        d.rounded_rectangle([mx-bw//2+2, my-bh//2+2, mx+bw//2+2, my+bh//2+2],
                             radius=4, fill=(185, 190, 210))
        d.rounded_rectangle([mx-bw//2, my-bh//2, mx+bw//2, my+bh//2],
                             radius=4, fill=(255, 255, 255), outline=col, width=2)
        d.text((mx, my), label, fill=col, font=fn_lbl, anchor='mm')

    # draw dashed (back) edges first, then solid (forward) on top
    for e in sorted(edges, key=lambda x: 0 if (len(x) > 4 and x[4]) else 1):
        draw_one(e[0], e[1], e[2], e[3], len(e) > 4 and bool(e[4]))

    # ── phone screen mockups (on top of arrows) ─────────────────
    for idx, (node_id, screen_lbl, content, cx, cy) in enumerate(nodes):
        x0, y0 = cx - SW // 2, cy - SH // 2
        x1, y1 = cx + SW // 2, cy + SH // 2

        d.rounded_rectangle([x0+4, y0+4, x1+4, y1+4], radius=16, fill=(175, 180, 200))
        d.rounded_rectangle([x0, y0, x1, y1], radius=16,
                             fill=(22, 22, 40), outline=(75, 80, 110), width=2)
        scx0, scy0, scx1, scy1 = x0+6, y0+24, x1-6, y1-12
        d.rectangle([scx0, scy0, scx1, scy1], fill=(250, 252, 255))
        d.rectangle([scx0, scy0, scx1, scy0+34], fill=(21, 101, 192))

        words = screen_lbl.split()
        ln1   = words[0]
        ln2   = ' '.join(words[1:]) if len(words) > 1 else ''
        if ln2:
            d.text((cx, scy0+11), ln1, fill=(255,255,255), font=fn_sm, anchor='mm')
            d.text((cx, scy0+24), ln2, fill=(200,230,255), font=fn_sm, anchor='mm')
        else:
            d.text((cx, scy0+17), ln1, fill=(255,255,255), font=fn_bd, anchor='mm')

        iy = scy0 + 42
        for i, line in enumerate(content):
            if iy + 15 > scy1 - 10: break
            d.rounded_rectangle([scx0+5, iy, scx1-5, iy+14], radius=3,
                                 fill=(210,220,240) if i%2==0 else (225,232,248))
            d.text((scx0+10, iy+2), line, fill=(35,45,80), font=fn_sm)
            iy += 20

        for di in (-20, 0, 20):
            d.ellipse([cx+di-3, scy1-12, cx+di+3, scy1-6], fill=(180,185,210))
        d.ellipse([cx-9, y0+6, cx+9, y0+20], fill=(10,10,30))
        d.rounded_rectangle([cx-16, y1-15, cx+16, y1-10], radius=2, fill=(75,80,110))

        bd = 22
        d.ellipse([x0-4, y0-4, x0-4+bd, y0-4+bd], fill=(220,40,40))
        d.text((x0-4+bd//2, y0-4+bd//2), str(idx+1), fill=(255,255,255), font=fn_bd, anchor='mm')

    # ── legend (dedup by color+label+dashed) ────────────────────
    leg_top = CH - LEGEND_H + 5
    d.rectangle([0, leg_top-5, CW, CH], fill=(232, 235, 245))
    d.line([(0, leg_top-5), (CW, leg_top-5)], fill=(200,205,220), width=1)
    d.text((20, leg_top+2), 'Arrow Legend:', fill=(40,40,60), font=fn_bd)

    seen_leg  = set()
    leg_items = []
    for e in edges:
        dsh = len(e) > 4 and bool(e[4])
        key = (e[3], e[2], dsh)
        if key not in seen_leg:
            seen_leg.add(key)
            leg_items.append((e[2], e[3], dsh))

    per_row = 6
    col_w   = max(150, (CW - 40) // min(len(leg_items), per_row))
    for i, (lbl, col, dsh) in enumerate(leg_items):
        ri  = i // per_row
        ci  = i % per_row
        lx  = 20 + ci * col_w
        ly  = leg_top + 22 + ri * 26
        if ly + 16 > CH: break
        if dsh:
            for seg in range(4):
                if seg % 2 == 0:
                    d.line([(lx+seg*9, ly+7), (lx+seg*9+6, ly+7)], fill=col, width=2)
        else:
            d.line([(lx, ly+7), (lx+32, ly+7)], fill=col, width=3)
        d.polygon([(lx+32, ly+3), (lx+42, ly+7), (lx+32, ly+11)], fill=col)
        d.text((lx+46, ly+7), lbl, fill=col, font=fn_sm, anchor='lm')

    buf = io.BytesIO()
    img.save(buf, format='PNG')
    buf.seek(0)
    scale = avail_w / CW
    return RLImage(buf, width=avail_w, height=CH * scale)


def user_guide_section():
    """Build the User Guide section with color-coded flow diagrams and step-by-step text."""
    items = []
    items.append(Paragraph("User Guide", ST_H1))
    items.append(HRFlowable(width='100%', thickness=1.5, color=C_PRIMARY, spaceAfter=10))
    items.append(Paragraph(
        "This section explains how to use JobLinker for both <b>Job Seekers</b> and "
        "<b>Employers</b>. Each diagram shows every screen as a phone mockup with a "
        "<b>unique-colored arrow</b> for each navigation path. The legend below each "
        "diagram identifies every arrow.",
        ST_BODY))
    items.append(Spacer(1, 10))

    av = PAGE_W - 3.6 * cm

    # ════════════════════════════════════════════════
    # JOB SEEKER FLOW
    # ════════════════════════════════════════════════
    items.append(Paragraph("Job Seeker — Screen Navigation Flow", ST_H2))
    items.append(Paragraph(
        "Follow the numbered screens from Splash to Call. "
        "Each arrow has a different color — see the legend at the bottom.",
        ST_BODY))
    items.append(Spacer(1, 6))

    #  (id, label, [content lines], cx, cy)
    sk_nodes = [
        (0, 'Splash',       ['Auto-check login',   'Redirect on start'],             620, 160),
        (1, 'Login',        ['Email / Password',   'Google Sign-In',  'Sign Up →'],  280, 390),
        (2, 'Register',     ['Name & Role',        'Email & Phone',   'Skills Bio'], 960, 390),
        (3, 'Jobs / Main',  ['Search bar',         'Quick-filter chips','Job cards'], 620, 620),
        (4, 'Filter Jobs',  ['Location',           'Employ. type',    'Salary'],     230, 850),
        (5, 'Job Details',  ['Title & Company',    'Salary range',    'Apply btn'],  620, 850),
        (6, 'Saved Jobs',   ['Bookmarked jobs',    'Remove / Open'],                 990, 850),
        (7, 'Applications', ['Pending',            'Shortlisted',     'Accepted'],   380, 1090),
        (8, 'Chat',         ['Send message',       'Voice note',      'Share file'], 700, 1090),
        (9, 'Call',         ['Video / Voice',      'Mute & Speaker',  'End call'],   700, 1310),
    ]

    # (from, to, label, color, dashed)
    # Solid colored  = forward navigation   Dashed gray = Back button
    _BK = (125, 125, 140)   # gray for all Back arrows
    sk_edges = [
        # ── Forward (solid, unique color) ──
        (0, 1, 'Not logged in',   ( 33, 150, 243), False),  # blue
        (0, 2, 'Not logged in',   (156,  39, 176), False),  # purple
        (1, 2, 'Sign Up',         (255, 152,   0), False),  # orange
        (1, 3, 'Login success',   ( 76, 175,  80), False),  # green
        (2, 3, 'Complete',        (  0, 150, 136), False),  # teal
        (3, 4, 'Filter icon',     (  0, 188, 212), False),  # cyan
        (3, 5, 'Tap job card',    ( 63,  81, 181), False),  # indigo
        (5, 6, 'Bookmark',        (121,  85,  72), False),  # brown
        (5, 7, 'Apply',           (103,  58, 183), False),  # deep purple
        (5, 8, 'Contact Employer',(233,  30,  99), False),  # pink
        (8, 9, 'Call button',     (244,  67,  54), False),  # red
        # ── Back / return (dashed gray) ──
        (1, 0, 'Back', _BK, True),
        (2, 1, 'Back', _BK, True),
        (4, 3, 'Back', _BK, True),
        (5, 3, 'Back', _BK, True),
        (6, 5, 'Back', _BK, True),
        (7, 5, 'Back', _BK, True),
        (8, 5, 'Back', _BK, True),
        (9, 8, 'Back', _BK, True),
    ]

    items.append(build_user_flow_image(av, 'Job Seeker Navigation Flow', sk_nodes, sk_edges))
    items.append(Paragraph(
        "Figure: Job Seeker flow — 10 screens, 11 uniquely colored arrows. "
        "Numbers = step order. See arrow legend for transition labels.",
        ST_CAPTION))
    items.append(Spacer(1, 10))

    # Job Seeker text steps
    sk_guide = [
        ("1. Splash Screen",
         "App launches SplashActivity. It automatically checks if you are already "
         "logged in (stored in SharedPreferences). If yes → skips to Jobs/Main. "
         "If not → redirects to Login."),
        ("2. Login / 3. Register",
         "LoginActivity offers Email+Password or Google Sign-In. New users tap "
         "'Sign Up' (orange arrow) to open RegisterActivity — a 3-step wizard: "
         "name & role → email & phone → skills & bio."),
        ("4. Jobs / Main",
         "MainActivity opens on the Jobs tab. A real-time search bar and quick-filter "
         "chips let you narrow results instantly. Pull to refresh for new listings."),
        ("5. Filter Jobs",
         "Tap the filter icon (cyan arrow) to open FilterActivity. "
         "Select location, employment type, and salary range, then tap Apply. "
         "Results update immediately in JobsFragment."),
        ("6. Job Details",
         "Tap any job card (indigo arrow) to open JobDetailsActivity. "
         "Read the full description and required skills. "
         "Tap 'Save' (brown arrow) to bookmark, or 'Apply' (deep purple) to submit."),
        ("7. Applications",
         "After applying, your application is tracked. "
         "Employers update the status to Shortlisted, Accepted, or Rejected. "
         "You receive a push notification on each status change."),
        ("8. Chat → 9. Call",
         "Tap 'Contact Employer' (pink arrow) on Job Details to open ChatActivity. "
         "Send text, voice notes, images, or files. "
         "Tap the call icon (red arrow) to start a voice or video call via Agora RTC."),
    ]
    for step_title, step_text in sk_guide:
        items.append(Paragraph(f"<b>{step_title}</b>", ST_H3))
        items.append(Paragraph(step_text, ST_BODY))
        items.append(Spacer(1, 4))

    items.append(PageBreak())

    # ════════════════════════════════════════════════
    # EMPLOYER FLOW
    # ════════════════════════════════════════════════
    items.append(Paragraph("Employer — Screen Navigation Flow", ST_H2))
    items.append(Paragraph(
        "Employers share Login, Register, and Chat/Call screens with Job Seekers, "
        "but gain exclusive access to Post Job and Applications Review.",
        ST_BODY))
    items.append(Spacer(1, 6))

    em_nodes = [
        (0, 'Splash',       ['Auto-check login',  'Redirect on start'],              550, 160),
        (1, 'Login',        ['Email / Password',  'Google Sign-In', 'Sign Up →'],    250, 400),
        (2, 'Register',     ['Name & Role',       'Company info',   'Password'],     850, 400),
        (3, 'Jobs / Main',  ['My posted jobs',    'FAB + button',   'Notifs'],       550, 650),
        (4, 'Post Job',     ['Job Title',         'Description',    'Skills/Salary'],200, 900),
        (5, 'Applications', ['Applicant list',    'Review profiles','Update status'],550, 900),
        (6, 'Edit Profile', ['Avatar upload',     'Company bio',    'Contact info'], 900, 900),
        (7, 'Chat',         ['Message seeker',    'Send files',     'Voice note'],   400, 1150),
        (8, 'Call',         ['Video / Voice',     'Mute & Speaker', 'End call'],     750, 1150),
    ]

    _BK = (125, 125, 140)
    em_edges = [
        # ── Forward (solid, unique color) ──
        (0, 1, 'Not logged in',   ( 33, 150, 243), False),  # blue
        (0, 2, 'Not logged in',   (156,  39, 176), False),  # purple
        (1, 2, 'Sign Up',         (255, 152,   0), False),  # orange
        (1, 3, 'Login success',   ( 76, 175,  80), False),  # green
        (2, 3, 'Complete',        (  0, 150, 136), False),  # teal
        (3, 4, 'FAB+ Post Job',   (255,  87,  34), False),  # deep orange
        (3, 5, 'View applicants', ( 63,  81, 181), False),  # indigo
        (3, 6, 'Edit Profile',    (121,  85,  72), False),  # brown
        (5, 7, 'Chat with seeker',(233,  30,  99), False),  # pink
        (7, 8, 'Call button',     (244,  67,  54), False),  # red
        # ── Back / return (dashed gray) ──
        (1, 0, 'Back', _BK, True),
        (2, 1, 'Back', _BK, True),
        (4, 3, 'Back', _BK, True),
        (5, 3, 'Back', _BK, True),
        (6, 3, 'Back', _BK, True),
        (7, 5, 'Back', _BK, True),
        (8, 7, 'Back', _BK, True),
    ]

    items.append(build_user_flow_image(av, 'Employer Navigation Flow', em_nodes, em_edges))
    items.append(Paragraph(
        "Figure: Employer flow — 9 screens, 10 uniquely colored arrows. "
        "Deep orange = Employer-only FAB. See arrow legend for all transition labels.",
        ST_CAPTION))
    items.append(Spacer(1, 10))

    em_guide = [
        ("1. Splash → Login / Register",
         "Same entry flow as Job Seeker. On RegisterActivity Step 1, select "
         "role = Employer to unlock employer-specific features."),
        ("2. Jobs / Main — Employer View",
         "MainActivity shows your posted jobs list instead of all jobs. "
         "An orange FAB '+' button is visible only to Employers. "
         "Notification badges show new applicants on each job card."),
        ("3. Post a Job",
         "Tap FAB '+' (deep orange arrow) to open PostJobActivity. "
         "Fill in title, description, required skills (chips), employment type, "
         "salary range, and location. Tap 'Post Job' — published instantly to Firestore."),
        ("4. Review Applications",
         "Tap any job card to see its Applications list (indigo arrow). "
         "Review each applicant's profile, skills, and cover letter. "
         "Update status to Shortlisted, Accepted, or Rejected — applicant is notified."),
        ("5. Chat & Call Applicants",
         "Tap 'Chat' on any applicant (pink arrow) to open ChatActivity. "
         "Discuss the role, request documents, or send voice notes. "
         "Tap the call icon (red arrow) to start a voice or video call."),
        ("6. Edit Profile",
         "Tap Edit Profile (brown arrow) from Jobs/Main to open EditProfileActivity. "
         "Upload a company logo, update the bio, and save contact details."),
    ]
    for step_title, step_text in em_guide:
        items.append(Paragraph(f"<b>{step_title}</b>", ST_H3))
        items.append(Paragraph(step_text, ST_BODY))
        items.append(Spacer(1, 4))

    items.append(Spacer(1, 12))

    # ── Features-by-role table ───────────────────────────────
    items.append(Paragraph("Features Available by Role", ST_H2))
    av2 = PAGE_W - 3.6 * cm
    feat_data = [
        [Paragraph("<b>Feature</b>",       ST_TABLE_HDR),
         Paragraph("<b>Job Seeker</b>",    ST_TABLE_HDR),
         Paragraph("<b>Employer</b>",      ST_TABLE_HDR)],
        [Paragraph("Browse Job Listings",    ST_TABLE_CELL), Paragraph("Yes", ST_TABLE_CELL), Paragraph("Yes", ST_TABLE_CELL)],
        [Paragraph("Apply for Jobs",         ST_TABLE_CELL), Paragraph("Yes", ST_TABLE_CELL), Paragraph("No",  ST_TABLE_CELL)],
        [Paragraph("Save / Bookmark Jobs",   ST_TABLE_CELL), Paragraph("Yes", ST_TABLE_CELL), Paragraph("No",  ST_TABLE_CELL)],
        [Paragraph("Post Job Listings",      ST_TABLE_CELL), Paragraph("No",  ST_TABLE_CELL), Paragraph("Yes", ST_TABLE_CELL)],
        [Paragraph("Review Applications",    ST_TABLE_CELL), Paragraph("No",  ST_TABLE_CELL), Paragraph("Yes", ST_TABLE_CELL)],
        [Paragraph("Real-time Chat",         ST_TABLE_CELL), Paragraph("Yes", ST_TABLE_CELL), Paragraph("Yes", ST_TABLE_CELL)],
        [Paragraph("Voice / Video Calls",    ST_TABLE_CELL), Paragraph("Yes", ST_TABLE_CELL), Paragraph("Yes", ST_TABLE_CELL)],
        [Paragraph("Edit Profile & Avatar",  ST_TABLE_CELL), Paragraph("Yes", ST_TABLE_CELL), Paragraph("Yes", ST_TABLE_CELL)],
        [Paragraph("Filter Job Search",      ST_TABLE_CELL), Paragraph("Yes", ST_TABLE_CELL), Paragraph("Yes", ST_TABLE_CELL)],
        [Paragraph("Push Notifications",     ST_TABLE_CELL), Paragraph("Yes", ST_TABLE_CELL), Paragraph("Yes", ST_TABLE_CELL)],
    ]
    ft = Table(feat_data, colWidths=[av2 * 0.50, av2 * 0.25, av2 * 0.25])
    ft.setStyle(TableStyle([
        ('BACKGROUND',    (0, 0), (-1, 0),  C_PRIMARY),
        ('TEXTCOLOR',     (0, 0), (-1, 0),  white),
        ('ROWBACKGROUNDS',(0, 1), (-1, -1), [C_SURFACE, white]),
        ('GRID',          (0, 0), (-1, -1), 0.4, C_DIVIDER),
        ('ALIGN',         (1, 0), (-1, -1), 'CENTER'),
        ('LEFTPADDING',   (0, 0), (-1, -1), 7),
        ('TOPPADDING',    (0, 0), (-1, -1), 5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
        ('VALIGN',        (0, 0), (-1, -1), 'MIDDLE'),
    ]))
    items.append(ft)

    return items


def uml_section():
    """Build the UML section: visual class diagram + relationship table."""
    from reportlab.platypus import Flowable
    import math

    # ── Visual UML Diagram ──────────────────────────────────
    class UMLDiagram(Flowable):
        """Draws a proper UML class diagram with boxes and arrows."""

        # ── Layout constants ──
        BW    = 218   # box width (pts)
        CGAP  = 45    # horizontal gap between columns
        CX1   = 0     # column-1 x
        CX2   = 263   # column-2 x  (BW + CGAP)
        HDR_H = 24    # class-name header height
        FH    = 13    # field row height
        FPAD  = 5     # field padding top/bottom
        RGAP  = 48    # vertical gap between rows

        # Colors (RGB 0-1)
        CLR_HDR   = (0.082, 0.396, 0.753)   # C_PRIMARY #1565C0
        CLR_HDR2  = (0.161, 0.502, 0.725)   # accent for stereotype
        CLR_BG    = (0.976, 0.980, 1.000)   # box bg
        CLR_BORD  = (0.700, 0.740, 0.820)   # box border
        CLR_TXT   = (0.118, 0.137, 0.196)   # field name
        CLR_TYPE  = (0.392, 0.431, 0.549)   # field type
        CLR_ARR   = (0.082, 0.396, 0.753)   # arrow color
        CLR_DASH  = (0.220, 0.600, 0.220)   # dashed arrow (uses)
        CLR_WHITE = (1, 1, 1)

        CLASSES = [
            # (name, [(field_name, type), ...], col, row)
            ('User', [
                ('+userId',     'String'),
                ('+userName',   'String'),
                ('+userEmail',  'String'),
                ('+userRole',   'String'),
                ('+userSkills', 'List<String>'),
                ('+isOnline',   'boolean'),
                ('+createdAt',  'long'),
            ], 0, 0),
            ('Job', [
                ('+jobId',        'String'),
                ('+jobTitle',     'String'),
                ('+employerId',   'String'),
                ('+employmentType','String'),
                ('+salaryMin',    'double'),
                ('+salaryMax',    'double'),
                ('+isActive',     'boolean'),
            ], 1, 0),
            ('Conversation', [
                ('+conversationId', 'String'),
                ('+participants',   'List<String>'),
                ('+lastMessage',    'String'),
                ('+unreadCount',    'int'),
            ], 0, 1),
            ('Application', [
                ('+applicationId', 'String'),
                ('+jobId',         'String'),
                ('+seekerUserId',  'String'),
                ('+status',        'String'),
                ('+coverLetter',   'String'),
                ('+appliedAt',     'long'),
            ], 1, 1),
            ('Message', [
                ('+messageId',   'String'),
                ('+convId',      'String'),
                ('+senderId',    'String'),
                ('+messageType', 'String'),
                ('+timestamp',   'long'),
            ], 0, 2),
            ('Call', [
                ('+callId',      'String'),
                ('+callerId',    'String'),
                ('+receiverId',  'String'),
                ('+callType',    'String'),
                ('+duration',    'long'),
            ], 1, 2),
        ]

        # Relationships: (from, to, label, from_card, to_card, style)
        # style: 'H'=horizontal, 'V'=vert-down, 'bent'=right-then-down
        RELATIONS = [
            ('User',         'Job',          'posts',         '1',  '0..*', 'H'),
            ('User',         'Conversation', 'participates',  '1',  '1..*', 'V'),
            ('Conversation', 'Message',      'contains',      '1',  '0..*', 'V'),
            ('Job',          'Application',  'receives',      '1',  '0..*', 'V'),
            ('User',         'Application',  'submits',       '1',  '0..*', 'bent_RA'),
            ('User',         'Call',         'initiates',     '1',  '0..*', 'bent_RC'),
        ]

        def box_h(self, n_fields):
            return self.HDR_H + 1 + self.FPAD + n_fields * self.FH + self.FPAD

        def __init__(self, avail_w):
            Flowable.__init__(self)
            # compute total height
            row_heights = [0, 0, 0]
            for _, fields, col, row in self.CLASSES:
                h = self.box_h(len(fields))
                row_heights[row] = max(row_heights[row], h)
            self._row_h = row_heights
            total = sum(row_heights) + 2 * self.RGAP + 30
            self.width  = avail_w
            self.height = total

        def _box_pos(self):
            """Return dict name->(bx, by_bottom, bh)."""
            positions = {}
            # row y-bottoms from BOTTOM of drawing:
            row_bot = [0, 0, 0]
            row_bot[2] = 15
            row_bot[1] = row_bot[2] + self._row_h[2] + self.RGAP
            row_bot[0] = row_bot[1] + self._row_h[1] + self.RGAP
            for name, fields, col, row in self.CLASSES:
                bx = self.CX1 if col == 0 else self.CX2
                by = row_bot[row]
                bh = self.box_h(len(fields))
                positions[name] = (bx, by, bh)
            return positions

        def _arrow(self, c, x1, y1, x2, y2, size=7):
            """Filled solid arrowhead at (x2,y2) pointing from (x1,y1)."""
            dx, dy = x2 - x1, y2 - y1
            ln = math.sqrt(dx*dx + dy*dy)
            if ln < 1:
                return
            ux, uy = dx/ln, dy/ln
            px, py = -uy, ux
            pts = [
                (x2, y2),
                (x2 - size*ux + size*0.38*px, y2 - size*uy + size*0.38*py),
                (x2 - size*ux - size*0.38*px, y2 - size*uy - size*0.38*py),
            ]
            path = c.beginPath()
            path.moveTo(*pts[0])
            path.lineTo(*pts[1])
            path.lineTo(*pts[2])
            path.close()
            c.drawPath(path, fill=1, stroke=0)

        def _label_box(self, c, cx, cy, text, font='Helvetica', size=7.5,
                       bg=(1,1,1), fg=(0.08,0.40,0.75)):
            """Draw a small white label box with text."""
            c.setFont(font, size)
            tw = c.stringWidth(text, font, size)
            pad = 3
            c.setFillColorRGB(*bg)
            c.setStrokeColorRGB(*self.CLR_BORD)
            c.setLineWidth(0.4)
            c.roundRect(cx - tw/2 - pad, cy - size/2 - 1,
                        tw + 2*pad, size + 4, 3, fill=1, stroke=1)
            c.setFillColorRGB(*fg)
            c.drawCentredString(cx, cy, text)

        def _cardinality(self, c, x, y, text):
            c.setFont('Helvetica-Bold', 7.5)
            c.setFillColorRGB(*self.CLR_ARR)
            c.drawCentredString(x, y, text)

        def draw(self):
            c = self.canv
            pos = self._box_pos()

            # ── Draw class boxes ────────────────────────────
            for name, fields, col, row in self.CLASSES:
                bx, by, bh = pos[name]

                # Drop shadow
                c.setFillColorRGB(0.75, 0.78, 0.84)
                c.roundRect(bx+3, by-3, self.BW, bh, 4, fill=1, stroke=0)

                # Box body
                c.setFillColorRGB(*self.CLR_BG)
                c.setStrokeColorRGB(*self.CLR_BORD)
                c.setLineWidth(1)
                c.roundRect(bx, by, self.BW, bh, 4, fill=1, stroke=1)

                # Header fill
                c.setFillColorRGB(*self.CLR_HDR)
                # Clip to top portion with rounded top
                c.rect(bx+1, by + bh - self.HDR_H, self.BW-2, self.HDR_H-1,
                       fill=1, stroke=0)
                # Re-draw top rounded portion
                c.roundRect(bx, by + bh - self.HDR_H, self.BW, self.HDR_H,
                             4, fill=1, stroke=0)

                # Stereotype <<class>>
                c.setFont('Helvetica', 6.5)
                c.setFillColorRGB(0.7, 0.88, 1.0)
                c.drawCentredString(bx + self.BW/2,
                                    by + bh - 9, '\xab\xabclass\xbb\xbb')

                # Class name
                c.setFont('Helvetica-Bold', 11)
                c.setFillColorRGB(*self.CLR_WHITE)
                c.drawCentredString(bx + self.BW/2,
                                    by + bh - self.HDR_H + 6, name)

                # Separator line after header
                c.setStrokeColorRGB(*self.CLR_BORD)
                c.setLineWidth(0.7)
                sep_y = by + bh - self.HDR_H - 1
                c.line(bx + 1, sep_y, bx + self.BW - 1, sep_y)

                # Field rows
                fy = sep_y - self.FPAD
                for i, (fname, ftype) in enumerate(fields):
                    fy -= self.FH
                    # Alternate row background
                    if i % 2 == 1:
                        c.setFillColorRGB(0.960, 0.965, 0.985)
                        c.rect(bx+1, fy-1, self.BW-2, self.FH, fill=1, stroke=0)
                    # Field name
                    c.setFont('Courier-Bold', 8)
                    c.setFillColorRGB(*self.CLR_TXT)
                    c.drawString(bx + 8, fy + 2, fname)
                    # Field type
                    c.setFont('Courier-Oblique', 7.5)
                    c.setFillColorRGB(*self.CLR_TYPE)
                    c.drawRightString(bx + self.BW - 6, fy + 2, ftype)

                # Bottom border re-draw
                c.setStrokeColorRGB(*self.CLR_BORD)
                c.setLineWidth(1)
                c.roundRect(bx, by, self.BW, bh, 4, fill=0, stroke=1)

            # ── Draw relationship arrows ─────────────────────
            c.setFillColorRGB(*self.CLR_ARR)
            c.setStrokeColorRGB(*self.CLR_ARR)

            for frm, to, label, card_from, card_to, style in self.RELATIONS:
                bx1, by1, bh1 = pos[frm]
                bx2, by2, bh2 = pos[to]
                mid1_y = by1 + bh1 / 2
                mid2_y = by2 + bh2 / 2
                # Arrow endpoints
                fr_right  = bx1 + self.BW
                fr_left   = bx1
                fr_top    = by1 + bh1
                fr_bot    = by1
                to_right  = bx2 + self.BW
                to_left   = bx2
                to_top    = by2 + bh2
                to_bot    = by2

                c.setLineWidth(1.3)
                c.setStrokeColorRGB(*self.CLR_ARR)
                c.setFillColorRGB(*self.CLR_ARR)

                if style == 'H':
                    # Horizontal: right edge of frm → left edge of to (same row)
                    x1, y1 = fr_right,  mid1_y
                    x2, y2 = to_left,   mid2_y
                    mid_y  = (y1 + y2) / 2
                    c.line(x1, y1, x2-6, y2)
                    self._arrow(c, x1, y1, x2, y2)
                    lx = (x1 + x2) / 2
                    self._label_box(c, lx, y1 + 12, label)
                    self._cardinality(c, x1 + 10, y1 + 6, card_from)
                    self._cardinality(c, x2 - 10, y2 + 6, card_to)

                elif style == 'V':
                    # Vertical down: bottom of frm → top of to (same col, frm above to)
                    x1 = bx1 + self.BW * 0.5
                    y1 = fr_bot
                    x2 = bx2 + self.BW * 0.5
                    y2 = to_top
                    # Draw a short perpendicular diamond at source (association)
                    c.line(x1, y1-6, x2, y2+6)
                    c.line(x1, y1, x2, y2+6)
                    self._arrow(c, x1, y1, x2, y2)
                    mid_x = x1 + 18
                    mid_y = (y1 + y2) / 2
                    self._label_box(c, mid_x + 18, mid_y, label)
                    self._cardinality(c, x1 + 10, y1 - 12, card_from)
                    self._cardinality(c, x2 + 10, y2 + 12, card_to)

                elif style == 'bent_RA':
                    # User → Application: right from User, bend down, then right to Application
                    # Jog: right 30 → down to Application mid y → right to Application left
                    jog_x  = fr_right + 22
                    jog_y1 = mid1_y - 10         # start going right a bit lower
                    jog_y2 = mid2_y               # Application mid
                    # Draw dashed line (different style)
                    c.setDash([5, 3], 0)
                    c.setStrokeColorRGB(*self.CLR_DASH)
                    c.setFillColorRGB(*self.CLR_DASH)
                    c.line(fr_right, jog_y1, jog_x, jog_y1)
                    c.line(jog_x, jog_y1, jog_x, jog_y2)
                    c.line(jog_x, jog_y2, to_left - 6, jog_y2)
                    c.setDash([], 0)
                    self._arrow(c, jog_x, jog_y2, to_left, jog_y2)
                    self._label_box(c, jog_x + 40, jog_y2 + 10, label,
                                    fg=(0.1, 0.55, 0.1))
                    self._cardinality(c, fr_right + 6, jog_y1 + 7, card_from)
                    self._cardinality(c, to_left - 14, jog_y2 + 7, card_to)

                elif style == 'bent_RC':
                    # User → Call: right from User, bend down all the way to Call row, then right
                    jog_x  = fr_right + 38
                    jog_y1 = mid1_y - 20
                    jog_y2 = mid2_y
                    c.setDash([4, 4], 0)
                    c.setStrokeColorRGB(*self.CLR_DASH)
                    c.setFillColorRGB(*self.CLR_DASH)
                    c.line(fr_right, jog_y1, jog_x, jog_y1)
                    c.line(jog_x, jog_y1, jog_x, jog_y2)
                    c.line(jog_x, jog_y2, to_left - 6, jog_y2)
                    c.setDash([], 0)
                    self._arrow(c, jog_x, jog_y2, to_left, jog_y2)
                    self._label_box(c, jog_x + 30, (jog_y1+jog_y2)/2, label,
                                    fg=(0.1, 0.55, 0.1))
                    self._cardinality(c, fr_right + 6,  jog_y1 + 7, card_from)
                    self._cardinality(c, to_left - 14, jog_y2 + 7, card_to)

            # ── Legend ──────────────────────────────────────
            lx, ly = self.CX2 + self.BW + 8, 20
            # Can't draw legend outside box width, skip if no space

    # ── Build section ────────────────────────────────────────
    items = []
    avail_w = PAGE_W - 3.6*cm

    # ── DIAGRAM 1: System Architecture (all 48 classes) ──────
    items.append(Paragraph("UML Class Diagram", ST_H1))
    items.append(HRFlowable(width='100%', thickness=1.5, color=C_PRIMARY, spaceAfter=10))
    items.append(Paragraph("Part 1 – System Architecture Overview", ST_H2))
    items.append(Paragraph(
        "The diagram below shows ALL 48 classes in the JobLinker project, organized by "
        "package. Dashed arrows indicate dependency relationships between packages. "
        "Each package is color-coded by its architectural role: "
        "<b>Blue</b> = Activities (Controllers), "
        "<b>Green</b> = Fragments (Views), "
        "<b>Purple</b> = Models (Entities), "
        "<b>Orange</b> = Firebase Layer, "
        "<b>Teal</b> = Adapters, "
        "<b>Red</b> = Services, "
        "<b>Violet</b> = Receivers, "
        "<b>Steel Blue</b> = Utilities.",
        ST_BODY))
    items.append(Spacer(1, 10))
    items.append(build_architecture_diagram(avail_w))
    items.append(Paragraph(
        "Figure 1: JobLinker Full System Architecture – All 48 Classes (9 packages)",
        ST_CAPTION))
    items.append(PageBreak())

    # ── DIAGRAM 2: Full Class Diagram (Pillow) ───────────────
    items.append(Paragraph("Part 2 – Full Class Diagram", ST_H2))
    items.append(Paragraph(
        "The diagram below shows all major classes across every layer: "
        "Firebase/Application, Activities, Fragments, and Data Models. "
        "Each box lists the class stereotype, fields (name : type), and methods. "
        "<b>Blue solid lines</b> = inheritance; "
        "<b>Purple dashed</b> = dependency/uses; "
        "<b>Orange dashed</b> = association/manages; "
        "<b>Green dashed</b> = hosting/contains.",
        ST_BODY))
    items.append(Spacer(1, 10))
    avail_w = PAGE_W - 3.6*cm
    items.append(build_full_uml_image(avail_w))
    items.append(Paragraph(
        "Figure 2: JobLinker Complete UML Class Diagram — 4 Layers, 24 Key Classes",
        ST_CAPTION))

    return items


def screen_section(screen_name, screen_data, page_num_hint=""):
    """Build a two-page layout for a screen: mockup + element table."""
    items = []
    items.append(Paragraph(screen_name, ST_H1))
    items.append(HRFlowable(width='100%', thickness=1.5, color=C_PRIMARY, spaceAfter=6))

    activity = screen_data.get('activity', '')
    if activity:
        items.append(Paragraph(
            f'<font size="9" color="#546E7A">Source File: <b>{activity}</b></font>',
            ST_BODY_SMALL))
    items.append(Spacer(1, 6))

    # Description
    items.append(Paragraph("Screen Overview", ST_H2))
    items.append(Paragraph(screen_data.get('description', ''), ST_BODY))
    items.append(Spacer(1, 10))

    # Two-column: phone + element table
    mockup_img = phone_image_flowable(
        screen_name, screen_data.get('elements', []), width=7*cm)

    ui_elements = screen_data.get('ui_elements', [])
    elem_table  = make_info_table(ui_elements,
                                  col_widths=[(PAGE_W-3.6*cm-8*cm)*0.36,
                                              (PAGE_W-3.6*cm-8*cm)*0.64])

    side_by_side = Table(
        [[mockup_img, elem_table]],
        colWidths=[7.5*cm, PAGE_W - 3.6*cm - 7.5*cm]
    )
    side_by_side.setStyle(TableStyle([
        ('VALIGN',      (0,0),(-1,-1),'TOP'),
        ('LEFTPADDING', (0,0),(0,-1), 0),
        ('RIGHTPADDING',(0,0),(0,-1), 12),
        ('LEFTPADDING', (1,0),(1,-1), 0),
    ]))
    items.append(side_by_side)
    items.append(Spacer(1, 6))
    items.append(Paragraph(
        f"Figure: {screen_name} – Phone Mockup",
        ST_CAPTION))

    return items


def source_code_section(name, code_text, description=""):
    items = []
    items.append(Paragraph(name, ST_H1))
    items.append(HRFlowable(width='100%', thickness=1.5, color=C_PRIMARY, spaceAfter=6))
    if description:
        items.append(Paragraph(description, ST_BODY))
        items.append(Spacer(1, 6))

    # Show full code
    items += section_code(f"{name}.java", code_text)
    return items


def reflection_section():
    items = []
    items.append(Paragraph("Self-Reflection & Conclusion", ST_H1))
    items.append(HRFlowable(width='100%', thickness=1.5, color=C_PRIMARY, spaceAfter=10))

    items.append(Paragraph("Personal Reflection", ST_H2))
    items.append(Paragraph(
        "Working on JobLinker for nearly a year was both challenging and deeply rewarding. "
        "This project pushed me well beyond the boundaries of what I had studied in the classroom, "
        "requiring me to independently research and implement technologies like Firebase Firestore "
        "real-time listeners, Agora voice calls, and multi-step form navigation — all while "
        "maintaining a consistent, professional user experience.",
        ST_BODY))
    items.append(Spacer(1, 8))

    items.append(Paragraph("Challenges Faced", ST_H2))
    challenges = [
        ("Real-time Chat",      "Implementing reliable real-time messaging with read receipts and typing indicators required careful Firestore listener management to avoid memory leaks and excessive reads."),
        ("Audio Recording",     "Recording and playing back voice messages involved managing MediaRecorder lifecycle, audio focus, and file uploads to Firebase Storage — each step requiring research and testing."),
        ("Role-based UI",       "Dynamically showing/hiding features based on user role (Employer vs. Job Seeker) required consistent state management throughout the entire app."),
        ("Firebase Indexes",    "Compound Firestore queries required creating composite indexes. Debugging FAILED_PRECONDITION errors taught me to plan data structure and queries together from the start."),
        ("Firestore Ordering",  "Combining orderBy with inequality filters (where + orderBy) required the first orderBy to match the inequality field — a subtle Firestore constraint that took debugging to discover."),
        ("Android 12+ Permissions", "Strict foreground service requirements on Android 12+ for CallService required adding FOREGROUND_SERVICE permission and proper notification channels."),
    ]
    for title, desc in challenges:
        items.append(Paragraph(f"<b>{title}:</b> {desc}", ST_BULLET))
        items.append(Spacer(1, 4))

    items.append(Spacer(1, 10))
    items.append(Paragraph("What I Learned", ST_H2))
    learned = [
        "End-to-end Android app development from zero to a production-ready application.",
        "Firebase ecosystem: Authentication, Firestore, Storage, and Cloud Messaging.",
        "Real-time communication patterns using Firestore listeners and Agora SDK.",
        "RecyclerView adapter patterns with DiffUtil for performance.",
        "Fragment lifecycle management and ViewPager2 navigation.",
        "Material Design principles applied to create a clean, professional UI.",
        "Debugging complex multi-threaded issues on Android.",
        "Version control with Git and maintaining a public GitHub repository.",
    ]
    for item in learned:
        items.append(Paragraph(f"• {item}", ST_BULLET))
        items.append(Spacer(1, 3))

    items.append(Spacer(1, 10))
    items.append(Paragraph("Future Improvements", ST_H2))
    improvements = [
        "Integrate AI-powered job matching that suggests relevant jobs based on the user's skills and experience.",
        "Add a video resume feature where job seekers can record and attach short video introductions to their applications.",
        "Implement a scheduling system for interviews directly within the app.",
        "Add employer analytics dashboard showing application rates, view counts, and conversion metrics.",
        "Expand to iOS using Kotlin Multiplatform or Flutter.",
    ]
    for item in improvements:
        items.append(Paragraph(f"• {item}", ST_BULLET))
        items.append(Spacer(1, 3))

    items.append(Spacer(1, 10))
    items.append(Paragraph("References", ST_H2))
    refs = [
        ("Firebase Documentation", "https://firebase.google.com/docs"),
        ("Agora RTC SDK for Android", "https://docs.agora.io/en/voice-calling/get-started/get-started-sdk"),
        ("Android Developer Guides", "https://developer.android.com/guide"),
        ("Material Design 3 Components", "https://m3.material.io/components"),
        ("Glide Image Loading", "https://bumptech.github.io/glide/"),
        ("RecyclerView with DiffUtil – Android Tutorial", "https://developer.android.com/reference/androidx/recyclerview/widget/DiffUtil"),
        ("ViewPager2 Guide", "https://developer.android.com/guide/navigation/navigation-swipe-view-2"),
        ("Firebase Firestore – Real-time Updates", "https://firebase.google.com/docs/firestore/query-data/listen"),
    ]
    for title, url in refs:
        items.append(Paragraph(f"• <b>{title}</b> — {url}", ST_BULLET))
        items.append(Spacer(1, 3))

    return items


# ─────────────────────────────────────────────
# MAIN BUILDER
# ─────────────────────────────────────────────

def build_book():
    out_path = r'C:\Users\emmaj\StudioProjects\JobLinker\JobLinker_Book.pdf'
    print("Loading source code...")
    with open(r'C:\Users\emmaj\StudioProjects\JobLinker\source_code.json',
              encoding='utf-8') as f:
        code = json.load(f)

    print("Initializing document...")
    doc = BookDocTemplate(
        out_path,
        pagesize=A4,
        title="JobLinker Project Book",
        author="Majd Ahmed Majdoub",
        subject="Android Application Documentation",
    )

    story = []

    # ── COVER ──────────────────────────────────────────────
    print("  Building cover...")
    story += build_cover()

    # ── TOC placeholder ─────────────────────────────────────
    toc = TableOfContents()
    toc.levelStyles = [ST_TOC1, ST_TOC2]
    story.append(Paragraph("Table of Contents", ST_H1))
    story.append(HRFlowable(width='100%', thickness=1.5, color=C_PRIMARY, spaceAfter=10))
    story.append(toc)
    story.append(PageBreak())

    # ── INTRODUCTION ────────────────────────────────────────
    print("  Introduction...")
    story += intro_section()
    story.append(PageBreak())

    story += quick_info_section()
    story.append(PageBreak())

    story += tech_stack_section()
    story.append(PageBreak())

    story += project_structure_section()
    story.append(PageBreak())

    story += uml_section()

    # ── APP SCREENS ─────────────────────────────────────────
    print("  Building screen pages...")
    story += section_divider("App Screens", "Phone Mockups & UI Element Descriptions")

    for screen_name, screen_data in SCREENS.items():
        print(f"    Screen: {screen_name}")
        story += screen_section(screen_name, screen_data)
        story.append(PageBreak())

    # ── USER GUIDE ──────────────────────────────────────────
    print("  User guide...")
    story += section_divider("User Guide", "Step-by-Step Instructions for Job Seekers & Employers")
    story += user_guide_section()
    story.append(PageBreak())

    # ── SOURCE CODE – MODELS ────────────────────────────────
    print("  Source code – Models...")
    story += section_divider("Source Code", "Models – Data Classes")

    model_descriptions = {
        "User":         "Represents a registered user in the system. Used for both Job Seeker and Employer accounts. Stored in the 'users' Firestore collection.",
        "Job":          "Represents a job posting created by an Employer. Contains all job details including skills, salary range, and applicant tracking. Stored in 'jobs' collection.",
        "Application":  "Represents a Job Seeker's application to a specific job. Tracks status (Applied → Shortlisted → Accepted/Rejected). Stored in 'applications' collection.",
        "Conversation": "Represents a one-to-one chat thread between two users. Stores the last message for preview and unread count. Stored in 'conversations' collection.",
        "Message":      "Represents a single message within a conversation. Supports multiple message types: text, image, audio, document, location, contact. Stored in 'messages' collection.",
        "Call":         "Represents a voice or video call record. Stores call type, status, and duration. Stored in 'calls' collection.",
    }
    for name in ["User","Job","Application","Conversation","Message","Call"]:
        if name in code:
            story += source_code_section(name, code[name], model_descriptions.get(name,""))
            story.append(PageBreak())

    # ── SOURCE CODE – FIREBASE ──────────────────────────────
    print("  Source code – Firebase...")
    story += section_divider("Source Code", "Firebase – Backend Integration")

    firebase_descriptions = {
        "AuthManager":            "Thin wrapper around FirebaseAuth providing sign-in, registration, Google auth, phone OTP, and password reset methods.",
        "DatabaseManager":        "Provides helper methods for common Firestore read/write patterns used across the app.",
        "JobLinkerFirebaseManager":"Main singleton Firebase manager. Contains all Firestore operations: CRUD for users, jobs, messages, conversations, applications, and real-time listeners.",
    }
    for name in ["AuthManager","DatabaseManager","JobLinkerFirebaseManager"]:
        if name in code:
            story += source_code_section(name, code[name], firebase_descriptions.get(name,""))
            story.append(PageBreak())

    # ── SOURCE CODE – ACTIVITIES ────────────────────────────
    print("  Source code – Activities...")
    story += section_divider("Source Code", "Activities – Screen Controllers")

    activity_descriptions = {
        "BaseActivity":             "Abstract base class for ALL activities. Handles language/locale switching via LocaleHelper in attachBaseContext(). All other activities extend this class.",
        "SplashActivity":           "Entry point of the app. Displays animated splash screen, checks login state, and routes to appropriate screen.",
        "LoginActivity":            "Hosts the login UI with two tabs (Email and Phone) using ViewPager2.",
        "RegisterActivity":         "Multi-step registration wizard with 3 steps using ViewPager2 and step indicators.",
        "MainActivity":             "Main hub with BottomNavigationView hosting Jobs, Chats, and Profile fragments.",
        "PostJobActivity":          "Job posting form for Employers. Validates and writes job data to Firestore.",
        "JobDetailsActivity":       "Detailed view of a job posting with apply, save, share, and report functionality.",
        "ChatActivity":             "Full-featured real-time messaging screen with support for text, image, voice, document, location, and contact messages.",
        "CallActivity":             "Voice and video call interface using Agora RTC SDK.",
        "EditProfileActivity":      "Profile editing form with avatar upload to Firebase Storage.",
        "FilterActivity":           "Advanced job filter screen. Returns FilterState to JobsFragment via ActivityResultLauncher.",
        "SettingActivity":          "App settings with account management, preferences, privacy, and support sections.",
        "SavedJobsActivity":        "Lists all jobs bookmarked by the user. Fetches from Firestore 'savedJobs' collection and renders with JobAdapter. Shows empty state when list is empty.",
        "EmailVerificationActivity":"Email verification flow post-registration.",
    }
    for name in ["BaseActivity","SplashActivity","LoginActivity","RegisterActivity","MainActivity",
                 "PostJobActivity","JobDetailsActivity","ChatActivity","CallActivity",
                 "EditProfileActivity","FilterActivity","SettingActivity",
                 "SavedJobsActivity","EmailVerificationActivity"]:
        if name in code:
            story += source_code_section(name, code[name], activity_descriptions.get(name,""))
            story.append(PageBreak())

    # ── SOURCE CODE – FRAGMENTS ─────────────────────────────
    print("  Source code – Fragments...")
    story += section_divider("Source Code", "Fragments – UI Panels")

    frag_descriptions = {
        "JobsFragment":          "Main job listing fragment with search, quick-filter chips, and real-time Firestore listener.",
        "ChatsFragment":         "Conversation list fragment with search and unread badge logic.",
        "ProfileFragment":       "User profile display with role-specific content (skills for seekers, job list for employers).",
        "LoginEmailFragment":    "Email/password login with Google Sign-In option.",
        "LoginPhoneFragment":    "Phone number + OTP verification login.",
        "RegisterStep1Fragment": "Step 1: basic user info and role selection.",
        "RegisterStep2Fragment": "Step 2: professional details and company info.",
        "RegisterStep3Fragment": "Step 3: creates Firebase user and saves profile to Firestore.",
    }
    for name in ["JobsFragment","ChatsFragment","ProfileFragment",
                 "LoginEmailFragment","LoginPhoneFragment",
                 "RegisterStep1Fragment","RegisterStep2Fragment","RegisterStep3Fragment"]:
        if name in code:
            story += source_code_section(name, code[name], frag_descriptions.get(name,""))
            story.append(PageBreak())

    # ── SOURCE CODE – ADAPTERS ──────────────────────────────
    print("  Source code – Adapters...")
    story += section_divider("Source Code", "Adapters – RecyclerView & Pager")

    adapter_descriptions = {
        "JobAdapter":          "RecyclerView adapter for job cards. Handles save/unsave toggle and click listeners.",
        "ConversationAdapter": "RecyclerView adapter for conversation list items. Shows avatar, name, last message, timestamp, and unread badge.",
        "MessageAdapter":      "RecyclerView adapter for chat messages. Handles 6 message types with sent/received variants.",
        "LoginPagerAdapter":   "ViewPager2 adapter for Login screen tabs (Email and Phone fragments).",
        "RegisterPagerAdapter":"ViewPager2 adapter for Register wizard (3 step fragments).",
    }
    for name in ["JobAdapter","ConversationAdapter","MessageAdapter",
                 "LoginPagerAdapter","RegisterPagerAdapter"]:
        if name in code:
            story += source_code_section(name, code[name], adapter_descriptions.get(name,""))
            story.append(PageBreak())

    # ── SOURCE CODE – SERVICES & RECEIVERS ─────────────────
    print("  Source code – Services & Receivers...")
    story += section_divider("Source Code", "Services, Receivers & Application")

    svc_descriptions = {
        "JobLinkerApplication":  "Custom Application subclass. Declared in AndroidManifest.xml as android:name. Initializes global app state and Firebase on app start.",
        "CallService":           "Foreground Service for active voice/video calls. Required on Android 12+ (API 31) to keep the call alive when the app is in the background. Shows a persistent notification with call status.",
        "JobLinkerFireBaseMessagingServices": "Extends FirebaseMessagingService. Receives FCM push notifications for new messages, job alerts, and call requests. Displays notifications using Android NotificationManager with custom channels.",
        "NetworkChangeReceiver": "BroadcastReceiver monitoring network connectivity changes (Wi-Fi / Mobile Data connected or disconnected). Updates the UI to warn the user when offline.",
        "BootReceiver":          "BroadcastReceiver for BOOT_COMPLETED. Re-registers any pending notifications or alarms after the device restarts.",
    }
    for name in ["JobLinkerApplication","CallService","JobLinkerFireBaseMessagingServices",
                 "NetworkChangeReceiver","BootReceiver"]:
        if name in code:
            story += source_code_section(name, code[name], svc_descriptions.get(name,""))
            story.append(PageBreak())

    # ── SOURCE CODE – UTILS ─────────────────────────────────
    print("  Source code – Utils...")
    story += section_divider("Source Code", "Utilities – Helper Classes")

    utils_descriptions = {
        "ValidationHelper":       "Provides static methods to validate email format, phone number, full name, and password strength.",
        "ImageUtils":             "Wraps Glide for loading circular and standard profile images from URLs. Handles null/empty URL gracefully.",
        "DateTimeHelper":         "Formats timestamps as relative time strings ('just now', '5 min ago', etc.) and absolute date strings.",
        "LocaleHelper":           "Manages app language switching. Applies locale to Activity context using attachBaseContext.",
        "LogoutHelper":           "Shows a confirmation dialog before logging out. Clears SharedPreferences and navigates to LoginActivity.",
        "SharedPreferencesManager":"Singleton manager for user session data: userId, userName, userRole, language, currency, login state.",
        "ProgressDialogHelper":   "Displays and dismisses a non-cancelable ProgressDialog overlay during long operations (photo upload, job submission, registration). Prevents double-taps.",
    }
    for name in ["ValidationHelper","ImageUtils","DateTimeHelper",
                 "LocaleHelper","LogoutHelper","SharedPreferencesManager","ProgressDialogHelper"]:
        if name in code:
            story += source_code_section(name, code[name], utils_descriptions.get(name,""))
            story.append(PageBreak())

    # ── CONCLUSION ──────────────────────────────────────────
    print("  Conclusion...")
    story += section_divider("Conclusion", "Self-Reflection & References")
    story += reflection_section()

    # ── BUILD ───────────────────────────────────────────────
    print("Building PDF (this may take a minute)...")
    doc.multiBuild(story)
    print(f"\nDONE! Book generated: {out_path}")
    return out_path


if __name__ == '__main__':
    build_book()
