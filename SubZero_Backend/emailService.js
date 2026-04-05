const nodemailer = require("nodemailer");
 
const transporter = nodemailer.createTransport({
    service: "gmail",
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS,
    },
});
 
async function sendPasswordResetEmail(toEmail, resetToken) {
    // In production this would be your real domain
    const resetLink = `${process.env.APP_BASE_URL}/auth/reset-password?token=${resetToken}`;
 
    await transporter.sendMail({
        from: `"SubZero" <${process.env.EMAIL_USER}>`,
        to: toEmail,
        subject: "Reset your SubZero password",
        html: `
            <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto;">
                <h2 style="color: #5B2EFA;">SubZero</h2>
                <p>Hi,</p>
                <p>We received a request to reset your password. Click the button below to choose a new one.</p>
                <a href="${resetLink}"
                   style="display: inline-block; margin: 24px 0; padding: 14px 28px;
                          background: linear-gradient(90deg, #5B2EFA, #C040FB);
                          color: white; text-decoration: none; border-radius: 8px;
                          font-weight: bold; font-size: 15px;">
                    Reset Password
                </a>
                <p style="color: #888; font-size: 13px;">
                    This link expires in <strong>1 hour</strong>. If you didn't request a reset,
                    you can safely ignore this email.
                </p>
                <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;" />
                <p style="color: #aaa; font-size: 12px;">© 2026 SubZero. All rights reserved.</p>
            </div>
        `,
    });
}
 
module.exports = { sendPasswordResetEmail };
 