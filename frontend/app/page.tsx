// app/page.tsx
import { redirect } from 'next/navigation';
import { getCurrentUser } from './actions/auth';
import AuthForm from './components/AuthForm';

export default async function Home() {
  const user = await getCurrentUser();
  
  if (user) {
    redirect('/dashboard');
  }

  return <AuthForm />;
}