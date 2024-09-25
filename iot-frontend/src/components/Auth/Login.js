import { useState } from "react";
import axios from "axios";

function Login(){

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const handleLogin = async (e) => {
        e.preventDefault();
        try{
            const response = await axios.post("http://localhost:8080/auth/login", {email, password});
            localStorage.setItem("token", response.data.token);
        }catch(err){
            console.error("Nieudane logowanie", err);
        }
    };

    return (
        <form onSubmit={handleLogin}>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" />
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Hasło" />
            <button type="submit">Zaloguj się</button>
        </form>
    );
}

export default Login;